/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
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
include "users.thrift"

namespace java com.siemens.sw360.datahandler.thrift.search
namespace php sw360.thrift.search

typedef users.User User

struct ResultDetail {
    1: required string key,
    2: optional string value
}

struct SearchResult {
    1: required string id,
    2: required string type
    3: required string name,
    4: required double score,
    5: optional list<ResultDetail> details
}

service SearchService {

    /**
     * return all documents that have properties starting with text, user is ignored
     **/
    list<SearchResult> search(1: required string text, 2: User user);

    /**
     *  return all documents of a type that is in the typeMask list and that have properties starting with text,
     *  user is ignored
     **/
    list<SearchResult> searchFiltered(1: required string text, 2: User user, 3: list<string> typeMask);
}
