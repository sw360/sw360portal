/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
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
package com.siemens.sw360.datahandler.common;

import com.google.common.base.*;
import com.google.common.base.Optional;
import com.google.common.collect.*;
import com.siemens.sw360.datahandler.thrift.DocumentState;
import com.siemens.sw360.datahandler.thrift.ModerationState;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.RequestSummary;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentType;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationRequest;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.users.UserService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.ektorp.DocumentOperationResult;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.log4j.LogManager.getLogger;

/**
 * @author Cedric.Bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 */
public class CommonUtils {

    private CommonUtils() {
        // Utility class with only static functions
    }

    private static final Ordering<String> CASE_INSENSITIVE_ORDERING = Ordering.from(String.CASE_INSENSITIVE_ORDER);

    public static final CSVFormat sw360CsvFormat = CSVFormat.RFC4180.withQuote('\'').withEscape('\\').withIgnoreSurroundingSpaces(true).withQuoteMode(QuoteMode.ALL);

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

    public static final Joiner COMMA_JOINER = Joiner.on(", ");

    private static final Predicate<String> NOT_EMPTY_OR_NULL = new Predicate<String>() {
        @Override
        public boolean apply(String input) {
            return !Strings.isNullOrEmpty(input);
        }
    };

    /**
     * Returns a sorted list containing the elements of the given collection.
     * The list is sorted alphabetically, ignoring case.
     */
    public static List<String> getSortedList(Collection<String> collection) {
        return collection != null ? CASE_INSENSITIVE_ORDERING.immutableSortedCopy(collection) : ImmutableList.<String>of();
    }

    public static String joinStrings(Iterable<String> strings) {
        return strings != null ? COMMA_JOINER.join(strings) : "";
    }

    public static Predicate<String> notEmptyOrNull() {
        return NOT_EMPTY_OR_NULL;
    }

    /**
     * Return true if and item is contained in a collection, false otherwise. Null objects make the function returns false
     */
    public static <T> boolean contains(T item, Collection<T> collection) {
        return item != null && collection != null && collection.contains(item);
    }

    public static <T> boolean contains(T item, T[] array) {
        return array != null && contains(item, ImmutableList.copyOf(array));
    }

    /**
     * Add a String to a set, if the string is not null
     */
    public static <T> void add(Collection<T> collection, T item) {
        if (collection != null && item != null) {
            collection.add(item);
        }
    }

    /**
     * Add all from right to left collection
     */
    public static <T> void addAll(Collection<T> left, Collection<T> right) {
        if (left != null && right != null) {
            left.addAll(right);
        }
    }

    public static <T> List<T> nullToEmptyList(List<T> in) {
        return in != null ? in : ImmutableList.<T>of();
    }


    public static <T> Collection<T> nullToEmptyCollection(Collection<T> in) {
        return in != null ? in : ImmutableList.<T>of();
    }

    public static <T> Set<T> nullToEmptySet(Set<T> in) {
        return in != null ? in : ImmutableSet.<T>of();
    }

    public static <K, V> Map<K, V> nullToEmptyMap(Map<K, V> in) {
        return in != null ? in : ImmutableMap.<K, V>of();
    }

    public static <T> Set<T> toSingletonSet(T in) {
        return in != null ? ImmutableSet.of(in) : ImmutableSet.<T>of();
    }

    public static String nullToEmptyString(Object in) {
        return in != null ? in.toString() : "";
    }

    public static Set<String> splitToSet(String value) {
        return ImmutableSet.copyOf(COMMA_SPLITTER.split(value));
    }

    /**
     * @param string string to convert to int
     * @return int value if input positive, negative on error
     */
    public static int toUnsignedInt(String string) {
        int integer = -1;
        try {
            integer = Integer.valueOf(string);
        } catch (NullPointerException | NumberFormatException ignored) { // sic.
        }
        return integer;
    }

    public static boolean oneIsNull(Object... objects) {
        for (Object object : objects) {
            if (object == null)
                return true;
        }
        return false;
    }

    public static boolean allAreEmptyOrNull(Collection... collections) {
        return !atLeastOneIsNotEmpty(collections);
    }
    public static boolean allAreEmptyOrNull(Map... maps) {
        return !atLeastOneIsNotEmpty(maps);
    }
    public static boolean allAreEmptyOrNull(String... strings) {
        return !atLeastOneIsNotEmpty(strings);
    }

    public static boolean atLeastOneIsNotEmpty(Collection... collections) {
        for (Collection collection : collections) {
                if(collection!=null && !collection.isEmpty()) return true;
        }
        return false;
    }

    public static boolean atLeastOneIsNotEmpty(Map... maps) {
        for (Map map : maps) {
            if(map!=null && !map.isEmpty()) return true;
        }
        return false;
    }

    public static boolean atLeastOneIsNotEmpty(String... strings) {
        for (String string : strings) {
            if(!Strings.isNullOrEmpty(string)) return true;
        }
        return false;
    }


    public static boolean allAreEmpty(Object... objects) {
        return !atLeastOneIsNotEmpty(objects);
    }

    public static boolean atLeastOneIsNotEmpty(Object... objects) {


        for (Object object : objects) {
            if (object instanceof Collection)
                if(!((Collection) object).isEmpty()) return true;
        }

        return false;
    }

    public static int compareAsNullsAreSmaller(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null ? 0 : -1;
        }
        if (o2 == null)
            return 1;

        return 0;
    }

    public static Optional<Attachment> getAttachmentOptional(final String attachmentId, Set<Attachment> attachments) {
        return Iterables.tryFind(attachments, new Predicate<Attachment>() {
            @Override
            public boolean apply(Attachment input) {
                return attachmentId.equals(input.getAttachmentContentId());
            }
        });
    }

    @NotNull
    public static Attachment getNewAttachment(User user, String attachmentContentId, String fileName) {
        Attachment attachment = new Attachment();
        attachment.setCreatedBy(user.getEmail());
        attachment.setCreatedOn(SW360Utils.getCreatedOn());
        attachment.setCreatedComment("");
        attachment.setFilename(fileName);
        attachment.setAttachmentContentId(attachmentContentId);
        attachment.setAttachmentType(AttachmentType.DOCUMENT);
        return attachment;
    }

    @NotNull
    public static Comparator<ModerationRequest> compareByTimeStampDescending() {
        return new Comparator<ModerationRequest>() {
            @Override
            public int compare(ModerationRequest o1, ModerationRequest o2) {
                return Long.compare(o2.getTimestamp(), o1.getTimestamp());
            }
        };
    }

    @NotNull
    public static DocumentState getOriginalDocumentState() {
        DocumentState documentState = new DocumentState().setIsOriginalDocument(true);
        documentState.unsetModerationState();
        return documentState;
    }

    public static Optional<ModerationRequest> getFirstModerationRequestOfUser(List<ModerationRequest> moderationRequestsForDocumentId, final String email) {
        return Iterables.tryFind(moderationRequestsForDocumentId, new Predicate<ModerationRequest>() {
            @Override
            public boolean apply(ModerationRequest input) {
                return input.getRequestingUser().equals(email);
            }
        });
    }

    @NotNull
    public static DocumentState getModeratedDocumentState(ModerationRequest moderationRequest) {
        DocumentState documentState = new DocumentState().setIsOriginalDocument(false);
        documentState.setModerationState(moderationRequest.getModerationState());
        return documentState;
    }

    public static boolean isStillRelevant(ModerationRequest request) {
        return request.getModerationState().equals(ModerationState.PENDING) || request.getModerationState().equals(ModerationState.INPROGRESS);
    }

    public static <T, V> AfterFunction<T, V> afterFunction(Function<V, T> function) {
        return new AfterFunction<>(function);
    }

    public static void closeQuietly(InputStream stream, Logger logger) {
        try {
            stream.close();
        } catch (IOException e) {
            logger.info("cannot close input stream", e);
        }
    }

    public static boolean isValidUrl(String url) {
        try {
            return !isNullOrEmpty(new URL(url).getHost());
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static String getTargetNameOfUrl(String url) {
        try {
            String path = new URL(url).getPath();
            String fileName = FilenameUtils.getName(path);
            return !isNullOrEmpty(fileName) ? fileName : path;
        } catch (MalformedURLException e) {
            return "";
        }
    }

    public static Boolean getBoolOrNull(String in) {
        if (!isNullOrEmpty(in)) {
            //elegance in redundancy :)
            if (in.equalsIgnoreCase("true")) return true;
            else if (in.equalsIgnoreCase("false")) return false;
        }
        return null;
    }


    public static Integer getIntegerOrNull(String in) {
        Integer out = null;
        if (!isNullOrEmpty(in)) {
            try {
                out = Integer.parseInt(in);
            } catch (NumberFormatException ignored) {
            }
        }
        return out;
    }

    public static <T extends Enum<T>> String getEnumStringOrNull(T val) {
        if (val != null) return val.name();
        return null;
    }

    public static Map<String, User> getStringUserMap(UserService.Iface userClient) throws TException {
        Map<String, User> userMap;
        userMap = Maps.uniqueIndex(userClient.getAllUsers(), new Function<User, String>() {
            @Override
            public String apply(User input) {
                return input.getEmail();
            }
        });
        return userMap;
    }

    public static void getMessageForRequestSummary(RequestSummary releaseRequestSummary, String typeInfo, StringBuilder stringBuilder) {
        if (releaseRequestSummary.isSetTotalAffectedElements() && releaseRequestSummary.isSetTotalElements()) {
            stringBuilder.append("Affected ").append(typeInfo).append(" elements: ")
                    .append(releaseRequestSummary.getTotalAffectedElements())
                    .append(" of ").append(releaseRequestSummary.getTotalElements())
                    .append(". ");
        }
    }

    public static RequestSummary prepareMessage(RequestSummary input, String info) {
        StringBuilder stringBuilder = new StringBuilder();
        if (input.isSetMessage()) {
            stringBuilder.append(input.message);
        }

        getMessageForRequestSummary(input, info, stringBuilder);
        input.setMessage(stringBuilder.toString());

        return input;
    }


    public static RequestSummary addToMessage(RequestSummary left, RequestSummary right, String info) {
        if (left.requestStatus.equals(RequestStatus.SUCCESS) && right.requestStatus.equals(RequestStatus.SUCCESS)) {
            left.setRequestStatus(RequestStatus.SUCCESS);
        } else {
            left.setRequestStatus(RequestStatus.FAILURE);
        }

        StringBuilder stringBuilder = new StringBuilder();
        if (left.isSetMessage()) {
            stringBuilder.append(left.message);
        }

        getMessageForRequestSummary(right, info, stringBuilder);
        left.setMessage(stringBuilder.toString());

        return left;
    }

    @NotNull
    public static RequestSummary addRequestSummaries(RequestSummary left, String typeInfoLeft, RequestSummary right, String typeInfoRight) {
        final RequestSummary requestSummary = prepareMessage(left, typeInfoLeft);
        return addToMessage(requestSummary, right, typeInfoRight);
    }

    public static ImmutableList<String> getAttachmentURLsFromAttachmentContents(List<AttachmentContent> attachmentContents) {
        return FluentIterable.from(attachmentContents).transform(new Function<AttachmentContent, String>() {
            @Override
            public String apply(AttachmentContent input) {
                return input.getRemoteUrl();
            }
        }).toList();
    }

    @NotNull
    public static Map<String, List<String>> getIdentifierToListOfDuplicates(ListMultimap<String, String> identifierToIds) {
        Map<String, List<String>> output = new HashMap<>();

        for (String identifier : identifierToIds.keySet()) {
            List<String> ids = identifierToIds.get(identifier);
            if (ids.size() > 1) {
                output.put(identifier, ids);
            }
        }

        return output;
    }

    @NotNull
    public static RequestSummary getRequestSummary(List<String> ids, List<DocumentOperationResult> documentOperationResults) {
        final RequestSummary requestSummary = new RequestSummary();
        if (documentOperationResults.isEmpty()) {
            requestSummary.setRequestStatus(RequestStatus.SUCCESS);
        } else {
            requestSummary.setRequestStatus(RequestStatus.FAILURE);
        }

        requestSummary.setTotalElements(ids.size());
        requestSummary.setTotalAffectedElements(ids.size() - documentOperationResults.size());
        return requestSummary;
    }

    public static Properties loadProperties(Class<?> clazz, String propertiesFilePath) {
        Properties props = new Properties();
        try {
            try (InputStream resourceAsStream = clazz.getResourceAsStream(propertiesFilePath)) {
                if (resourceAsStream == null)
                    throw new IOException("cannot open " + propertiesFilePath);

                props.load(resourceAsStream);
            }
        } catch (IOException e) {
            getLogger(clazz).error("Error opening resource " + propertiesFilePath + ", switching to default.", e);
        }
        return props;
    }

    public static <T> T getFirst(Iterable<T> iterable) {
        final Iterator<T> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            throw new NoSuchElementException();
        }
    }

    public static class AfterFunction<T, V> {
        private Function<V, T> transformer;

        private AfterFunction(Function<V, T> transformer) {
            this.transformer = transformer;
        }

        public Predicate<V> is(Predicate<T> predicate) {
            return Predicates.compose(predicate, transformer);
        }
    }
}
