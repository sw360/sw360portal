#!/usr/bin/python


# This is a manual database migration script. It is assumed that a dedicated migration framework will be written in the future.
# When that happens, this script should be refactored to conform to the framework's prerequisites to be run by the framework.
# For example, server address and db name should be parametrized, the code reorganized into a single class or function, etc.


import couchdb

COUCHSERVER = "http://localhost:5984/"
DBNAME = 'sw360db'

couch=couchdb.Server(COUCHSERVER)
db = couch[DBNAME]

licenses_by_id_fun = '''function(doc){
    if (doc.type=="license"){
        emit(doc._id, doc)
    }
}'''

linking_objects_fun = '''function(doc){
    if ((doc.type=="release" || doc.type=="component") && doc.mainLicenseIds && doc.mainLicenseIds.length > 0){
        emit(doc._id, doc)
    }
}'''

licenses = db.query(licenses_by_id_fun)
linking_objects = db.query(linking_objects_fun)


print 'Updating external links to licenses'
for linking_obj_row in linking_objects:
    linking_obj = linking_obj_row.value
    linking_obj['mainLicenseIds'] = map(lambda license_id: licenses[license_id].rows[0].value['shortname'], linking_obj['mainLicenseIds'])
    if 'mainLicenseNames' in linking_obj:
        del linking_obj['mainLicenseNames']
    db.save(linking_obj)

print 'Copying licenses to new ids'
for license_row in licenses:
    license = license_row.value
    db.delete(license)
    del license['_rev']
    del license['shortname']
    license['_id'] = license['shortname']
    db.save(license)
