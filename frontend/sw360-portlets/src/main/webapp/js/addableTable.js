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

$( document ).ready(function() {
    var imgPath = contextpath;
    $('table.addableRows').each( function() {
            var theTable = $(this);
            theTable.after(  $('<button>Add</button>').click(function () {
                var tr    = theTable.find('tr').last();
                var clone = tr.clone();

                clone.find('td').each( function () {
                    var td = $(this);
                    td.children('input, select, textarea').each(
                        function () {
                            var c  =  $(this);
                            ['id', 'name'].forEach(function(matcher) {
                                changeAttribute (c,matcher);
                            });
                        }
                    );
                    td.children('label').each(
                        function () {
                            var c  =  $(this);
                            ['for'].forEach(function(matcher) {
                                changeAttribute (c,matcher);
                            });
                        }
                    );
                });

                clone.find(':text').val('');
                clone.find(':file').val('');
                clone.find('td.deletor').remove(); //for deletableRows
                tr.after(clone);

                initializeDelRows(imgPath); //for deletableRows
                return false;
            }));
        }
    );

    initializeDelRows(imgPath);
});

//+
//    '  <img onClick="deleteVendorRow(' + vendorcount + ');" src="' + contextpath + '/images/ic_delete.png" style="cursor: pointer;" class="trashicon"/>' +
//'     .after( $('<td class="deletor" hidden="" width="3%"><img src="' + imgPath + '/images/ic_delete.png" style="cursor: pointer;" class="trashicon"/></td>')

function initializeDelRows(imgPath){


    $('table.deletableRows').each( function() {
            var theTable = $(this);
            theTable.find('tbody tr').each(function() {
                var theRow= $(this);
                theRow.find('td.deletor').remove();
                theRow.find('td').last()
                    .after( $('<td class="deletor" hidden="" width="3%"><button>Del</button></td>')
                        .click(function () {
                            $(this).closest('tr').remove();
                            hideOrShowDeletors(theTable);
                        })
                     );
            });
            hideOrShowDeletors(theTable);
        }
    );
}

function hideOrShowDeletors(theTable) {
    var trc    = theTable.last('tr .tr_clone').find('tr').length;

    var minlength=3;
    if (theTable.hasClass("noHeadTable")) minlength=2;

    if(trc < minlength) theTable.find('td.deletor').each( function( ) {$(this).find('button').each(function () { $(this).prop('disabled', true);} ); $(this).hide();   } );
    else theTable.find('td.deletor').each( function( ) {$(this).find('button').each(function () { $(this).prop('disabled', false);} ); $(this).show();   } );
}

function changeAttribute (c,matcher) {
    var oldID = c.attr(matcher);
    var idPref  = oldID.match(/[A-Za-z]+/ig);
    var oldIdNr = oldID.match(/\d+/g);
    oldIdNr = oldIdNr?oldIdNr:"0";
    var newIdNr = 1+parseInt(oldIdNr);

    c.attr(matcher,idPref+newIdNr );
}