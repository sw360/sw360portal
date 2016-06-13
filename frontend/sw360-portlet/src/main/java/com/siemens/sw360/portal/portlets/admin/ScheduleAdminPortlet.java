/*
 * Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
 * With contributions by Bosch Software Innovations GmbH, 2016.
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

import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.RequestSummary;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.fossology.FossologyHostFingerPrint;
import com.siemens.sw360.datahandler.thrift.schedule.ScheduleService;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.portal.common.UsedAsLiferayAction;
import com.siemens.sw360.portal.portlets.Sw360Portlet;
import com.siemens.sw360.portal.users.UserCacheHolder;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import javax.portlet.*;
import java.io.IOException;
import java.util.List;

public class ScheduleAdminPortlet extends Sw360Portlet {

    private static final Logger log = Logger.getLogger(ScheduleAdminPortlet.class);


    @Override
    public void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        List<FossologyHostFingerPrint> fingerPrints;

        super.doView(request, response);
    }

    @UsedAsLiferayAction
    public void scheduleCveSearch(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        try {
            User user = UserCacheHolder.getUserFromRequest(request);
            RequestSummary requestSummary =
                    new ThriftClients().makeScheduleClient().scheduleService(ThriftClients.CVESEARCH_SERVICE, user);
            setSessionMessage(request, requestSummary.getRequestStatus(), "Task", "schedule");
        } catch (TException e){
            log.error(e);
        }
    }

    @UsedAsLiferayAction
    public void unscheduleCveSearch(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        try {
            User user = UserCacheHolder.getUserFromRequest(request);
            RequestStatus requestStatus =
                    new ThriftClients().makeScheduleClient().unscheduleService(ThriftClients.CVESEARCH_SERVICE, user);
            setSessionMessage(request, requestStatus, "Task", "unschedule");
        } catch (TException e){
            log.error(e);
        }
    }

    @UsedAsLiferayAction
    public void unscheduleAllServices(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        try {
            User user = UserCacheHolder.getUserFromRequest(request);
            RequestStatus requestStatus =
                    new ThriftClients().makeScheduleClient().unscheduleAllServices(user);
            setSessionMessage(request, requestStatus, "Every task", "unschedule");
        } catch (TException e){
            log.error(e);
        }
    }
}
