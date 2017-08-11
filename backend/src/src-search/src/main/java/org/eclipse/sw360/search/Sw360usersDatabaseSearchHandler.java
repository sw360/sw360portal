package org.eclipse.sw360.search;

import org.eclipse.sw360.datahandler.common.DatabaseSettings;
import org.eclipse.sw360.datahandler.thrift.search.SearchResult;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.search.db.AbstractDatabaseSearchHandler;

import java.io.IOException;

public class Sw360usersDatabaseSearchHandler extends AbstractDatabaseSearchHandler {

    public Sw360usersDatabaseSearchHandler() throws IOException {
        super(DatabaseSettings.COUCH_DB_USERS);
    }

    @Override
    protected boolean isVisibleToUser(SearchResult result, User user) {
        return true;
    }

}
