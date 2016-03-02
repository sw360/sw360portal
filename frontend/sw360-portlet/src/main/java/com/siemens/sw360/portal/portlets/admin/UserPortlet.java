/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License Version 2.0 as published by the
 * Free Software Foundation with classpath exception.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License version 2.0 for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (please see the COPYING file); if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */
package com.siemens.sw360.portal.portlets.admin;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.portlet.PortletResponseUtil;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.model.*;
import com.liferay.portal.model.User;
import com.liferay.portal.service.*;
import com.liferay.portal.service.persistence.RoleUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.users.*;
import com.siemens.sw360.datahandler.thrift.users.UserGroup;
import com.siemens.sw360.datahandler.thrift.users.UserService;
import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.common.UsedAsLiferayAction;
import com.siemens.sw360.portal.portlets.Sw360Portlet;
import com.siemens.sw360.portal.users.UserCSV;
import com.siemens.sw360.portal.users.UserCacheHolder;
import com.siemens.sw360.portal.users.UserUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import javax.portlet.*;
import java.io.*;
import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.siemens.sw360.portal.users.UserUtils.getRoleConstantFromUserGroup;

/**
 * Created by jn on 06.03.15.
 *
 * @author johannes.najjar@tngtech.com
 */
public class UserPortlet extends Sw360Portlet {
    private static final Logger log = Logger.getLogger(UserPortlet.class);

    @Override
    public void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {

        List<com.siemens.sw360.datahandler.thrift.users.User> missingUsers = new ArrayList<>();
        List<com.siemens.sw360.datahandler.thrift.users.User> backEndUsers;

        List<User> liferayUsers;
        List<User> liferayUsers2;
        try {
            liferayUsers2 = UserLocalServiceUtil.getUsers(QueryUtil.ALL_POS, QueryUtil.ALL_POS);
        } catch (SystemException e) {
            log.error("Could not get user List from liferay", e);
            liferayUsers2 = Collections.emptyList();
        }
        liferayUsers = FluentIterable.from(liferayUsers2).filter(new Predicate<User>() {
            @Override
            public boolean apply(User liferayUser) {

                String firstName = liferayUser.getFirstName();
                String lastName = liferayUser.getLastName();
                String emailAddress = liferayUser.getEmailAddress();
                List<Organization> organizations;
                try {
                    organizations = liferayUser.getOrganizations();
                } catch (PortalException | SystemException e) {
                    return false;
                }

                String department = "";

                if (organizations != null && organizations.size() > 0) {
                    department = organizations.get(0).getName();
                }

                String userGroup = "";

                List<Role> roles;
                try {
                    roles = liferayUser.getRoles();
                } catch (SystemException e) {
                    return false;
                }
                List<String> roleNames = new ArrayList<>();

                for (Role role : roles) {
                    roleNames.add(role.getName());
                }

                for (UserGroup group : UserGroup.values()) {
                    String roleConstantFromUserGroup = getRoleConstantFromUserGroup(group);
                    if (roleNames.contains(roleConstantFromUserGroup)) {
                        userGroup = group.toString();
                        break;
                    }
                }

                String gid = liferayUser.getOpenId();
                String passwordHash = liferayUser.getPassword();

                return !(isNullOrEmpty(firstName) || isNullOrEmpty(lastName) || isNullOrEmpty(emailAddress) || isNullOrEmpty(department) || isNullOrEmpty(userGroup) || isNullOrEmpty(gid) || isNullOrEmpty(passwordHash));
            }
        }).toList();


        try {
            UserService.Iface client = thriftClients.makeUserClient();
            backEndUsers = CommonUtils.nullToEmptyList(client.searchUsers(null));
        } catch (TException e) {
            log.error("Problem with user client", e);
            backEndUsers = Collections.emptyList();
        }


        if (backEndUsers.size() > 0) {

            final ImmutableSet<String> lifeRayMails = FluentIterable.from(liferayUsers).transform(new Function<User, String>() {
                @Override
                public String apply(User input) {
                    return input.getEmailAddress();
                }
            }).toSet();

            missingUsers = FluentIterable.from(backEndUsers).filter(new Predicate<com.siemens.sw360.datahandler.thrift.users.User>() {
                @Override
                public boolean apply(com.siemens.sw360.datahandler.thrift.users.User input) {
                    return !lifeRayMails.contains(input.getEmail());
                }
            }).toList();

        }

        request.setAttribute(PortalConstants.USER_LIST, liferayUsers);
        request.setAttribute(PortalConstants.MISSING_USER_LIST, missingUsers);

        // Proceed with page rendering
        super.doView(request, response);
    }


    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws IOException, PortletException {
        String action = request.getParameter(PortalConstants.ACTION);

        if (PortalConstants.USER_LIST.equals(action)) {

            try {
                backUpUsers(request, response);
            } catch (SystemException | PortalException e) {
                log.error("Something went wrong with the user backup", e);
            }
        }
    }


    public void backUpUsers(ResourceRequest request, ResourceResponse response) throws PortletException, IOException, SystemException, PortalException {
        List<User> liferayUsers;
        try {
            liferayUsers = UserLocalServiceUtil.getUsers(QueryUtil.ALL_POS, QueryUtil.ALL_POS);
        } catch (SystemException e) {
            log.error("Could not get user List from liferay", e);
            liferayUsers = Collections.emptyList();
        }


        final ByteArrayOutputStream outB = new ByteArrayOutputStream();
        Writer out = new BufferedWriter(new OutputStreamWriter(outB));

        CSVPrinter csvPrinter = new CSVPrinter(out, CommonUtils.sw360CsvFormat);

        csvPrinter.printRecord("GivenName", "Lastname", "Email", "Department", "UserGroup", "GID", "isMale", "PasswdHash","wantsMailNotification");
        for (User liferayUser : liferayUsers) {

            String firstName = liferayUser.getFirstName();
            String lastName = liferayUser.getLastName();
            String emailAddress = liferayUser.getEmailAddress();
            List<Organization> organizations = liferayUser.getOrganizations();

            String department = "";

            if (organizations != null && organizations.size() > 0) {
                department = organizations.get(0).getName();
            }

            /*String userGroup = "";

            List<Role> roles = liferayUser.getRoles();
            List<String> roleNames = new ArrayList<>();

            for (Role role : roles) {
                roleNames.add(role.getName());
            }

            for (UserGroup group : UserGroup.values()) {
                String roleConstantFromUserGroup = getRoleConstantFromUserGroup(group);
                if (roleNames.contains(roleConstantFromUserGroup)) {
                    userGroup = group.toString();
                    break;
                }
            }*/

            String gid = liferayUser.getOpenId();
            boolean isMale = liferayUser.isMale();
            String passwordHash = liferayUser.getPassword();
            if (isNullOrEmpty(emailAddress) || isNullOrEmpty(department))
                continue;
            com.siemens.sw360.datahandler.thrift.users.User sw360user = UserCacheHolder.getUserFromEmail(emailAddress);
            boolean wantsMailNotification =
                    sw360user.isSetWantsMailNotification() ? sw360user.wantsMailNotification : true;

            String userGroup = sw360user.getUserGroup().toString();

            log.info("out:"+ csvPrinter.getOut().toString());
            csvPrinter.printRecord(firstName, lastName, emailAddress, department, userGroup, gid, isMale, passwordHash, wantsMailNotification);
        }

        csvPrinter.flush();
        csvPrinter.close();

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(outB.toByteArray());
        PortletResponseUtil.sendFile(request, response, "Users.csv", byteArrayInputStream, "text/csv");
    }

    @UsedAsLiferayAction
    public void updateUsers(ActionRequest request, ActionResponse response) throws PortletException, IOException {

        List<UserCSV> users;
        try {
            users = getUsersFromRequest(request, "file");
        } catch (TException e) {
            log.error("Error processing csv file", e);
            users = Collections.emptyList();
        }

        try {
            createOrganizations(request, users);
        } catch (SW360Exception | SystemException | PortalException e) {
            log.error("Error creating organizations", e);
        }

        for (UserCSV user : users) {
            dealWithUser(request, user);
        }
    }

    private String extractHeadDept(String input) {
        String[] split = input.split(" ");
        if (split.length > 1) {
            return split[0] + " " + split[1];
        } else return split[0];

    }

    private void createOrganizations(PortletRequest request, List<UserCSV> users) throws SW360Exception, SystemException, PortalException {

        /* Find the departments of the users, create the head departments and then create the organizations */

        ImmutableSet<String> headDepartments = FluentIterable.from(users).transform(new Function<UserCSV, String>() {
            @Override
            public String apply(UserCSV input) {
                return extractHeadDept(input.getDepartment());
            }
        }).toSet();

        Map<String, Long> organizationIds = new HashMap<>();
        ServiceContext serviceContext = ServiceContextFactory.getInstance(request);
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        long companyId = themeDisplay.getCompanyId();
        for (String headDepartment : headDepartments) {

            long organizationId;
            try {
                organizationId = OrganizationLocalServiceUtil.getOrganizationId(companyId, headDepartment);
            } catch (SystemException e) {
                organizationId = 0;
            }

            if (organizationId == 0) { // The organization does not yet exist
                Organization organization = createOrganization(serviceContext, headDepartment, OrganizationConstants.DEFAULT_PARENT_ORGANIZATION_ID);

                organizationId = organization.getOrganizationId();
            }
            organizationIds.put(headDepartment, organizationId);
        }

        ImmutableSet<String> departments = FluentIterable.from(users).transform(new Function<UserCSV, String>() {
            @Override
            public String apply(UserCSV input) {
                return input.getDepartment();
            }
        }).toSet();


        for (String department : departments) {
            long organizationId;
            try {
                organizationId = OrganizationLocalServiceUtil.getOrganizationId(companyId, department);
            } catch (SystemException e) {
                organizationId = 0;
            }
            if (organizationId == 0) { // The organization does not yet exist
                createOrganization(serviceContext, department, organizationIds.get(extractHeadDept(department)).intValue());
            }
        }
    }

    private Organization createOrganization(ServiceContext serviceContext, String headDepartment, int parentId) throws PortalException, SystemException {
        return OrganizationServiceUtil.addOrganization(
                parentId,
                headDepartment,
                OrganizationConstants.TYPE_REGULAR_ORGANIZATION,
                RegionConstants.DEFAULT_REGION_ID,
                CountryConstants.DEFAULT_COUNTRY_ID,
                ListTypeConstants.ORGANIZATION_STATUS_DEFAULT,
                "",
                false,
                serviceContext
        );
    }

    private User getCurrentUser(PortletRequest request) throws SW360Exception {
        User user;
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);

        if (themeDisplay.isSignedIn())
            user = themeDisplay.getUser();
        else {
            throw new SW360Exception("Broken portlet!");
        }
        return user;
    }

    public static User addLiferayUser(PortletRequest request, String firstName, String lastName, String emailAddress, String organizationName, String roleName, boolean male, String openId, String passwordHash) throws SystemException, PortalException {
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        long companyId = themeDisplay.getCompanyId();

        long organizationId = OrganizationLocalServiceUtil.getOrganizationId(companyId, organizationName);
        final Role role = RoleLocalServiceUtil.getRole(companyId, roleName);
        long roleId = role.getRoleId();

        return addLiferayUser(request, firstName, lastName, emailAddress, organizationId, roleId, male, openId, passwordHash);
    }

    /**
     * Copied from https://github.com/fdelprete/CSV_User_Import-portlet/blob/master/docroot/WEB-INF/src/com/fmdp/csvuserimport/portlet/UserServiceImpl.java
     * with slight modifications
     *
     * @author Filippo Maria Del Prete
     * <p/>
     * based on the original work of Paul Butenko
     * http://java-liferay.blogspot.it/2012/09/how-to-make-users-import-into-liferay.html
     */
    public static User addLiferayUser(PortletRequest request, String firstName, String lastName, String emailAddress, long organizationId, long roleId, boolean male, String openId, String passwordHash) {
        User user = null;
        try {
            ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);

            long creatorUserId = themeDisplay.getUserId();
            long companyId = themeDisplay.getCompanyId();

            boolean autoPassword = false;
            String password1 = passwordHash;
            String password2 = passwordHash;
            boolean autoScreenName = false;
            String screenName = firstName + lastName;
            String middleName = "";
            long facebookId = 0;

            Locale locale = themeDisplay.getLocale();
            int prefixId = 0;
            int suffixId = 0;
            int birthdayMonth = 1;
            int birthdayDay = 1;
            int birthdayYear = 1970;
            String jobTitle = "";
            long[] groupIds = null;
            long[] organizationIds = null;

            if (organizationId != 0) {
                organizationIds = new long[1];
                organizationIds[0] = organizationId;
            }

            long[] roleIds = null;
            if (roleId != 0) {
                roleIds = new long[1];
                roleIds[0] = roleId;
            }
            long[] userGroupIds = null;

            boolean sendEmail = false;

            ServiceContext serviceContext = ServiceContextFactory.getInstance(request);
            user = null;
            boolean userbyscreeenname_exists = true;
            boolean userbyemail_exists = true;

            try {
                user = UserLocalServiceUtil.getUserByScreenName(companyId, screenName);
            } catch (NoSuchUserException nsue) {
                userbyscreeenname_exists = false;
            }
            try {
                user = UserLocalServiceUtil.getUserByEmailAddress(companyId, emailAddress);
            } catch (NoSuchUserException nsue) {
                userbyemail_exists = false;
            }

            if ((!userbyscreeenname_exists) & (!userbyemail_exists)) {
                user = UserLocalServiceUtil.addUser(creatorUserId,
                        companyId,
                        autoPassword,
                        password1,
                        password2,
                        autoScreenName,
                        screenName,
                        emailAddress,
                        facebookId,
                        openId,
                        locale,
                        firstName,
                        middleName,
                        lastName,
                        prefixId,
                        suffixId,
                        male,
                        birthdayMonth,
                        birthdayDay,
                        birthdayYear,
                        jobTitle,
                        groupIds,
                        organizationIds,
                        roleIds,
                        userGroupIds,
                        sendEmail,
                        serviceContext);
                user.setPasswordReset(false);

                user.setPassword(passwordHash);
                user.setPasswordEncrypted(true);

                Role role = RoleLocalServiceUtil.getRole(roleId);
                RoleUtil.addUser(role.getRoleId(), user.getUserId());
                UserLocalServiceUtil.updateUser(user);
                RoleLocalServiceUtil.updateRole(role);


                UserLocalServiceUtil.updateStatus(user.getUserId(), WorkflowConstants.STATUS_APPROVED);
                Indexer indexer = IndexerRegistryUtil.getIndexer(User.class);

                indexer.reindex(user);
            } else {
                String msg_exists = "";
                if (userbyscreeenname_exists) {
                    msg_exists = msg_exists + "Screen Name is not unique.";
                }
                if (userbyemail_exists) {
                    msg_exists = msg_exists + " Email Address is not unique.";
                }
                log.debug(msg_exists);
                user = null;
            }
        } catch (PortalException | SystemException e) {
            log.error(e);
        }
        return user;
    }


    private List<UserCSV> getUsersFromRequest(PortletRequest request, String fileUploadFormId) throws IOException, TException {

        final UploadPortletRequest uploadPortletRequest = PortalUtil.getUploadPortletRequest(request);

        final InputStream stream = uploadPortletRequest.getFileAsStream(fileUploadFormId);
        Reader reader = new InputStreamReader(stream);
        CSVFormat format = CommonUtils.sw360CsvFormat;
        CSVParser parser = new CSVParser(reader, format);
        List<CSVRecord> records;
        records = parser.getRecords();
        if (records.size() > 0) {
            records.remove(0); // Remove header
        }

        return getUsersFromCSV(records);

    }

    private List<UserCSV> getUsersFromCSV(List<CSVRecord> records) {
        List<UserCSV> users = new ArrayList<>();

        for (CSVRecord record : records) {
            try {
                UserCSV user = new UserCSV(record);
                users.add(user);
            } catch (IndexOutOfBoundsException e) {
                log.error("Broken csv record");
            }
        }

        return users;
    }

    private User dealWithUser(PortletRequest request, UserCSV userRec) {
        User user = null;
        try {
            user = userRec.addLifeRayUser(request);
            if (user != null) {
                UserUtils.synchronizeUserWithDatabase(userRec, thriftClients);
            }
        } catch (SystemException | PortalException e) {
            log.error("Error creating a new user", e);
        }

        return user;
    }
}
