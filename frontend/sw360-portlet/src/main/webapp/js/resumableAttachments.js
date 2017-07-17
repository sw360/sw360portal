/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * SPDX-License-Identifier: EPL-1.0
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

var FileMap = function () {
    var map = {};

    var Entry = function(file, value) {
        return {
            file: file,
            value: value
        }
    };

    var pr = {
        getId: function (file) {
            return file.uniqueIdentifier;
        }
    };

    return {
        get: function (file) {
            var id = pr.getId(file);
            var entry = map[id];
            return entry ? entry.value : null;
        },
        put: function (file, element) {
            var id = pr.getId(file);
            map[id] = new Entry(file, element);
        },
        remove: function (file) {
            var id = pr.getId(file);
            delete map[id];
        },
        isEmpty: function () {
            for (var id in map) {
                if (map.hasOwnProperty(id)) {
                    return false;
                }
            }
            return true;
        },
        each: function (callback) {
            if (typeof callback !== 'function') {
                return;
            }
            for (var id in map) {
                if (map.hasOwnProperty(id)) {
                    var entry = map[id];
                    callback(entry.file, entry.value);
                }
            }
        }
    }
};

var ResumableAttachments = function (resumable, $container) {
    var fileControllers = new FileMap();
    var callbacks = {};

    var $fileControllers = $("<div></div>").appendTo($container);

    $("<button>Upload</button>").appendTo($container).click(function () {
        resumable.upload();
    });
    $("<button>Pause</button>").appendTo($container).click(function () {
        resumable.pause();
    });

    var doCallback = function(name, file) {
        if (callbacks.hasOwnProperty(name)) {
            var callback = callbacks[name];
            if (typeof callback == 'function') {
                callback(file);
            }
        }
    };

    var removeFile = function (file) {
        fileControllers.get(file).$fileController.remove();
        fileControllers.remove(file);
        resumable.removeFile(file);
    };

    var addFile = function (file) {
        var $fileController = $("<div></div>").appendTo($fileControllers);

        $("<span></span>").text(file.fileName + " (" + file.size + "b)").appendTo($fileController);

        $("<button>Restart</button>").appendTo($fileController).click(function () {
            file.retry();
        });
        $("<button>Cancel</button>").appendTo($fileController).click(function () {
            file.abort();
            doCallback('fileCancel', file);
            removeFile(file);
        });

        var $progress = $("<div></div>").appendTo($fileController)
            .progressbar({
                max: 1
            });

        var controller = {
            $fileController: $fileController,
            $progress: $progress
        };

        fileControllers.put(file, controller);
    };

    var drawFileProgress = function (file) {
        var controller = fileControllers.get(file);
        controller.$progress.progressbar("value", file.progress());
    };

    return {
        drawFileProgress: drawFileProgress,
        addFile: addFile,
        removeFile: removeFile,
        isEmpty: function () {
            return fileControllers.isEmpty();
        },
        on: function (eventName, callback) {
            callbacks[eventName] = callback;
        }
    }

};