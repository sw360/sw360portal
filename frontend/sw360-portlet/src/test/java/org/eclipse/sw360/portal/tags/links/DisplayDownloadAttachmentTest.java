package org.eclipse.sw360.portal.tags.links;

import org.eclipse.sw360.datahandler.thrift.attachments.Attachment;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class DisplayDownloadAttachmentTest {

    private Attachment getAttachment(String name){
        Attachment attachment = new Attachment();
        attachment.setFilename(name);
        attachment.setAttachmentContentId(name);
        return attachment;
    }

    @Test
    public void setIdsTest_null() throws Exception {
        DisplayDownloadAttachment displayDownloadAttachment = new DisplayDownloadAttachment();
        displayDownloadAttachment.setIds(null);
        assertThat(displayDownloadAttachment.ids, not(is(nullValue())));
    }

    @Test
    public void setIdsTest_emptyList() throws Exception {
        DisplayDownloadAttachment displayDownloadAttachment = new DisplayDownloadAttachment();
        displayDownloadAttachment.setIds(Collections.emptyList());
        assertThat(displayDownloadAttachment.ids, not(is(nullValue())));
        assertThat(displayDownloadAttachment.ids.size(), is(0));
    }

    @Test
    public void setIdsTest_emptySet() throws Exception {
        DisplayDownloadAttachment displayDownloadAttachment = new DisplayDownloadAttachment();
        displayDownloadAttachment.setIds(Collections.emptySet());
        assertThat(displayDownloadAttachment.ids, not(is(nullValue())));
        assertThat(displayDownloadAttachment.ids.size(), is(0));
    }

    @Test
    public void setIdsTest_singletonList() throws Exception {
        DisplayDownloadAttachment displayDownloadAttachment = new DisplayDownloadAttachment();
        displayDownloadAttachment.setIds(Collections.singletonList(getAttachment("test1")));
        assertThat(displayDownloadAttachment.ids, not(is(nullValue())));
        assertThat(displayDownloadAttachment.ids.size(), is(1));
        assertThat(displayDownloadAttachment.ids.stream().findAny().get(), is("test1"));
    }

    @Test
    public void setIdsTest_List() throws Exception {
        List<Attachment> attachments = new ArrayList<>();
        attachments.add(getAttachment("test1"));
        attachments.add(getAttachment("test2"));

        DisplayDownloadAttachment displayDownloadAttachment = new DisplayDownloadAttachment();
        displayDownloadAttachment.setIds(attachments);
        assertThat(displayDownloadAttachment.ids, not(is(nullValue())));
        assertThat(displayDownloadAttachment.ids.size(), is(2));
        assertThat(displayDownloadAttachment.ids, containsInAnyOrder(attachments.stream()
                .map(a -> a.attachmentContentId)
                .collect(Collectors.toList())
                .toArray(new String[attachments.size()])
        ));
    }

    @Test
    public void setIdsTest_Set() throws Exception {
        Set<Attachment> attachments = new HashSet<>();
        attachments.add(getAttachment("test1"));
        attachments.add(getAttachment("test2"));

        DisplayDownloadAttachment displayDownloadAttachment = new DisplayDownloadAttachment();
        displayDownloadAttachment.setIds(attachments);
        assertThat(displayDownloadAttachment.ids, not(is(nullValue())));
        assertThat(displayDownloadAttachment.ids.size(), is(2));
        assertThat(displayDownloadAttachment.ids, containsInAnyOrder(attachments.stream()
                .map(a -> a.attachmentContentId)
                .collect(Collectors.toList())
                .toArray(new String[attachments.size()])
        ));
    }
}