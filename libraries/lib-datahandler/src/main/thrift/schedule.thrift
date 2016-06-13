/*
 * Copyright (c) Bosch Software Innovations GmbH 2016.
 * Part of the SW360 Portal Project.
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


include "sw360.thrift"
include "users.thrift"

namespace java com.siemens.sw360.datahandler.thrift.schedule
namespace php sw360.thrift.schedule

typedef sw360.RequestStatus RequestStatus
typedef sw360.RequestSummary RequestSummary
typedef users.User User

service ScheduleService {
    /*
     * a service with service name is scheduled
     * serviceName has to be registered in ThriftClients
     * service must provide an "update" method
     *
     * user has to be admin, otherwise FAILURE is returned
     */
    RequestSummary scheduleService(1: string serviceName, 2: User user);

    /*
     * all tasks with  name serviceName are cancelled
     * user has to be admin, otherwise FAILURE is returned
     */
    RequestStatus unscheduleService(1: string serviceName, 2: User user);

    /*
     * all scheduled tasks are cancelled
     * user has to be admin, otherwise FAILURE is returned
     */
    RequestStatus unscheduleAllServices(1: User user);
}
