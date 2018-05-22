/*
 * Copyright Siemens AG, 2017.
 * Part of the SW360 Portal Project.
 *
 * SPDX-License-Identifier: EPL-1.0
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

function Modal() {

    var _this = this;

    this.opened = false;
    this.modalBox = undefined;
    this.modalContent = undefined;
    this.modalOverlay = undefined;

    this.init = function () {
        var modalBox = document.createElement("div");
        modalBox.style.display = "none";
        modalBox.style.position = "fixed";
        modalBox.style.left = "50%";
        modalBox.style.top = "50%";
        modalBox.style.transform = "translateX(-50%) translateY(-50%)";
        modalBox.style.zIndex = "999999";
        modalBox.style.border = "1px solid #c5c5c5";
        modalBox.style.backgroundColor = "#ffffff";
        modalBox.style.color = "#333333";
        modalBox.style.padding = "3em 0.2em 2em 0.2em";
        modalBox.style.width = "40%";
        modalBox.style.height = "40%";
        _this.modalBox = modalBox;

        var modalClose = document.createElement("button");
        modalClose.style.position = "absolute";
        modalClose.style.right = "0.2em";
        modalClose.style.top = "0.2em";
        modalClose.style.width = "20px";
        modalClose.style.height = "20px";
        modalClose.style.position = "absolute";
        modalClose.style.backgroundImage = "url('/sw360-portlet/webjars/jquery-ui/1.12.1/images/ui-icons_777777_256x240.png')";
        modalClose.style.backgroundPosition = "-95px -128px";
        modalClose.style.border = "1px solid #c5c5c5";
        modalClose.style.borderRadius = "0.2em";
        _this.modalClose = modalClose;

        var modalContent = document.createElement("div");
        modalContent.style.width = "100%";
        modalContent.style.height = "100%";
        modalContent.style.overflowY = "scroll";
        modalContent.style.display = "flex";
        modalContent.style.justifyContent = "center";
        _this.modalContent = modalContent;

        modalBox.appendChild(modalClose);
        modalBox.appendChild(modalContent);
        document.body.appendChild(modalBox);

        modalClose.onclick = _this.close;
    };

    this.open = function (content) {
        if (content) {
            _this.modalContent.innerHTML = "";
            _this.modalContent.appendChild(content);
        } else {
            _this.setLoader();
        }

        _this.modalBox.style.display = "block";
        _this.modalOverlay = document.createElement("div");
        _this.modalOverlay.style.zIndex = "100";
        _this.modalOverlay.setAttribute("class", "ui-widget-overlay ui-front");
        document.body.appendChild(_this.modalOverlay);
        _this.opened = true;
    };

    this.setLoader = function () {
        _this.modalContent.innerHTML = "";
        var loader = document.createElement("img");
        loader.style.display = "flex";
        loader.style.width = "3em";
        loader.style.height = "3em";
        loader.style.margin = "4em 0";
        loader.setAttribute("src", "/sw360-portlet/images/loader.gif");
        _this.modalContent.appendChild(loader);
    };

    this.refresh = function (content) {
        _this.modalContent.innerHTML = "";
        _this.modalContent.appendChild(content);
    };

    this.close = function () {
        _this.modalBox.style.display = "none";
        _this.modalContent.innerHTML = "";
        _this.modalOverlay.remove();
        _this.opened = false;
    };

    this.isOpened = function () {
        return _this.opened;
    };

    _this.init();
}

define("modules/codeScoop", [], function() {
    function codeScoop(apiUrl, apiToken) {

        var _this = this;

        this.apiUrl = apiUrl;
        this.apiToken = apiToken;
        this.formElements = {
            form: undefined,
            name: undefined,
            categories: undefined,
            homepage: undefined,
            blog: undefined,
            wiki: undefined,
            mailing: undefined,
            description: undefined,
            componentList: undefined,
            componentListUrl: undefined
        };
        this.indexTable = undefined;
        this.indexData = {};
        this.timeout = undefined;
        this.indexDone = false;
        this.interval = 500;
        this.modal = new Modal();

        this._api = function (method, path, request, callback) {
            var xhr = new XMLHttpRequest();
            xhr.open(method, _this.apiUrl + path, true);
            xhr.setRequestHeader("X-Api-Key", _this.apiToken);
            xhr.setRequestHeader("X-User-Login", "test");
            xhr.setRequestHeader("Content-type", "application/json");
            xhr.onreadystatechange = function() {
                if (xhr.readyState === 4 && xhr.status === 200) {
                    var dto = JSON.parse(this.responseText);
                    callback(dto);
                }
            };
            xhr.send(request);
        };

        this._fetch_repo = function (owner, name, callback) {
            _this._api("GET", "repository/" + owner + "/" + name + "/", null, callback);
        };

        this._fetch_composite = function (requestData, callback) {
            _this._api("POST", "integration/siemens/composite", JSON.stringify(requestData), callback);
        };

        this._fetch_releases = function (id, callback) {
            _this._api("GET", "integration/siemens/releases?gitHubRepoId=" + id, null, callback);
        };

        this._install_form_element = function () {
            _this.formElements.form = document.getElementsByTagName("form")[0];
            var inputs = _this.formElements.form.getElementsByTagName("input");
            for (var i = 0; i < inputs.length; i++) {
                if (inputs[i].name.indexOf("NAME") > 0) {
                    _this.formElements.name = inputs[i];
                }
                if (inputs[i].name.indexOf("CATEGORIES") > 0) {
                    _this.formElements.categories = inputs[i];
                }
                if (inputs[i].name.indexOf("HOMEPAGE") > 0) {
                    _this.formElements.homepage = inputs[i];
                }
                if (inputs[i].name.indexOf("BLOG") > 0) {
                    _this.formElements.blog = inputs[i];
                }
                if (inputs[i].name.indexOf("WIKI") > 0) {
                    _this.formElements.wiki = inputs[i];
                }
                if (inputs[i].name.indexOf("MAILINGLIST") > 0) {
                    _this.formElements.mailing = inputs[i];
                }
                if (inputs[i].name.indexOf("MAILINGLIST") > 0) {
                    _this.formElements.mailing = inputs[i];
                }
            }
            var textAreas = _this.formElements.form.getElementsByTagName("textarea");
            for (var i = 0; i < textAreas.length; i++) {
                if (textAreas[i].name.indexOf("DESCRIPTION") > 0) {
                    _this.formElements.description = textAreas[i];
                }
            }
        };

        this._build_autocomplete_box = function (element) {
            var list = document.createElement("div");
            list.id = "codescoop-autocomplete";
            list.style.display = "none";
            list.style.height = "auto";
            list.style.minHeight = "20px";
            list.style.color = "#555";
            list.style.backgroundColor = "#FFF";
            list.style.fontSize = "14px";
            list.style.fontWeight = "100";
            list.style.padding = "4px 6px";
            list.style.border = "1px solid #DDD";
            list.style.position = "absolute";
            list.style.zIndex = "99999999";

            var rect = element.getBoundingClientRect(),
                scrollLeft = window.pageXOffset || document.documentElement.scrollLeft,
                scrollTop = window.pageYOffset || document.documentElement.scrollTop;

            list.style.left = parseInt(rect.left + scrollLeft).toString() + "px";
            list.style.top = parseInt(rect.top + scrollTop + element.offsetHeight).toString() + "px";
            list.style.width = parseInt(element.offsetWidth - 13).toString() + "px";

            return list;
        };

        this._install_autocomplete_box = function () {
            _this.formElements.componentList = _this._build_autocomplete_box(_this.formElements.name);
            document.body.appendChild(_this.formElements.componentList);
            _this.formElements.componentListUrl = _this._build_autocomplete_box(_this.formElements.homepage);
            document.body.appendChild(_this.formElements.componentListUrl);
        };

        this._autocomplete_repo = function (searchType, searchValue, limit, callback) {
            var xhr = new XMLHttpRequest();
            xhr.open("GET", _this.apiUrl + "autocomplete?data=" + searchType + "&limit=" + limit + "&search=" + searchValue, true);
            xhr.setRequestHeader("X-Api-Key", _this.apiToken);
            xhr.setRequestHeader("X-User-Login", "test");
            xhr.onreadystatechange = function() {
                if (xhr.readyState === 4 && xhr.status === 200) {
                    var repoDtoList = JSON.parse(this.responseText);
                    callback(repoDtoList);
                }
            };
            xhr.send(null);
        };

        this._listen_autocomplete = function () {

            var autoFillForm = function(id) {
                var ownerName = id.split("/");
                _this._fetch_repo(ownerName[0], ownerName[1], function (repo) {
                    _this.formElements.name.value = id;
                    _this.formElements.description.innerHTML = repo.title;
                    _this.formElements.homepage.value = repo.url;
                    _this.formElements.blog.value = repo.url;
                    _this.formElements.wiki.value = repo.url;
                    _this.formElements.mailing.value = repo.url;
                    _this.formElements.categories.value = repo.categories.join(',');
                })
            };

            var clean = function() {
                _this.formElements.componentList.removeEventListener("click", select);
                _this.formElements.componentList.innerHTML = "";
                _this.formElements.componentList.style.display = "none";

                _this.formElements.componentListUrl.removeEventListener("click", select);
                _this.formElements.componentListUrl.innerHTML = "";
                _this.formElements.componentListUrl.style.display = "none";
            };

            var select = function(e) {
                if (e.target.nodeName === "P") {
                    var data = e.target.getAttribute("data").split(":");
                    
                    if (data[0] === "name") {
                        _this.formElements.name.value = data[1];
                    } else if (data[0] === "url") {
                        _this.formElements.homepage.value = e.target.innerHTML;
                    }

                    clean();
                    autoFillForm(data[1]);
                }
            };

            var nameChanged = function () {
                if (_this.formElements.name.value.length < 2) {
                    return;
                }

                _this._autocomplete_repo("name", _this.formElements.name.value, 5, function (repoList) {
                    clean();
                    if (repoList.length > 0) {
                        for (var i = 0; i < repoList.length; i++) {
                            var repoID = repoList[i].owner + "/" + repoList[i].name;
                            var div = document.createElement("div");
                            var p = document.createElement("p");
                            p.innerHTML = repoID;
                            p.setAttribute("data", "name:" + repoID);

                            div.style.cursor = "pointer";
                            div.style.margin = "0 0 3px 0";

                            div.appendChild(p);
                            _this.formElements.componentList.appendChild(div);
                        }

                        _this.formElements.componentList.style.display = "block";
                        _this.formElements.componentList.addEventListener("click", select);
                    }
                });
            };
            _this.formElements.name.addEventListener("keyup", nameChanged);
            _this.formElements.name.onpaste = nameChanged;

            var homepageChanged = function () {
                if (_this.formElements.homepage.value.length < 2) {
                    return;
                }

                _this._autocomplete_repo("url", _this.formElements.homepage.value, 5, function (repoList) {
                    clean();
                    if (repoList.length > 0) {
                        for (var i = 0; i < repoList.length; i++) {
                            var repoID = repoList[i].owner + "/" + repoList[i].name;
                            var div = document.createElement("div");
                            var p = document.createElement("p");
                            p.innerHTML = repoList[i].url;
                            p.setAttribute("data", "url:" + repoID);

                            div.style.cursor = "pointer";
                            div.style.margin = "0 0 3px 0";

                            div.appendChild(p);
                            _this.formElements.componentListUrl.appendChild(div);
                        }

                        _this.formElements.componentListUrl.style.display = "block";
                        _this.formElements.componentListUrl.addEventListener("click", select);
                    }
                });
            };
            _this.formElements.homepage.addEventListener("keyup", homepageChanged);
            _this.formElements.homepage.onpaste = homepageChanged;
        };

        this._init_release_button = function () {
            var releaseButton = document.createElement("button");
            releaseButton.innerHTML = "Populate release";
            releaseButton.setAttribute("class","addButton");
            releaseButton.style.cursor = "pointer";
            releaseButton.style.cssFloat = "right";

            document.getElementById("ComponentBasicInfo")
                .getElementsByTagName("thead")[0]
                .getElementsByTagName("th")[0]
                .appendChild(releaseButton);

            var selectRepoFromList = function (e) {
                if (e.target.nodeName === "BUTTON") {
                    _this.modal.setLoader();
                    var id = e.target.getAttribute("data");
                    _this._fetch_releases(id, function (dto) {
                        console.log(dto);
                        var languageInput = document.getElementById("programminglanguages");
                        var langs = dto.repo.langs;
                        for (var i = 0; i < langs.length; i++) {
                            if (i !== 0) {
                                languageInput.value = languageInput.value + ", ";
                            }
                            languageInput.value = languageInput.value + langs[i];
                        }

                        var table = document.createElement("div");
                        table.style.height = "auto";
                        table.style.width = "80%";

                        var componentReleasesKey = Object.keys(dto.releases);

                        for (var i = 0; i < componentReleasesKey.length; i++) {
                            var name = componentReleasesKey[i];
                            if (dto.releases[name][0].releaseSource === "RELEASE_GITHUB") {
                                var ownerName = name.split("/");
                                if (componentReleasesKey.indexOf(ownerName[1]) > -1) {
                                    componentReleasesKey.splice(i, 1);
                                    componentReleasesKey.splice(componentReleasesKey.indexOf(ownerName[1]));
                                    componentReleasesKey.unshift(ownerName[1]);
                                } else {
                                    componentReleasesKey.splice(i, 1);
                                    componentReleasesKey.unshift(name);
                                }
                                break;
                            }
                        }

                        for (var i = 0; i < componentReleasesKey.length; i++) {
                            var row = document.createElement("div");
                            row.style.margin = "5px 0";
                            row.style.borderBottom = "1px solid rgb(197, 197, 197)";
                            row.innerHTML = "<strong>" + componentReleasesKey[i] + "</strong>";
                            table.appendChild(row);

                            var releases = dto.releases[componentReleasesKey[i]];

                            for (var j = 0; j < releases.length; j++) {
                                var rel = releases[j];

                                var arow = document.createElement("div");
                                arow.style.display = "flex";
                                arow.style.flexDirection = "row";
                                arow.style.justifyContent = "space-between";
                                arow.style.margin = "5px 0";

                                var date = rel.dateUTC;
                                var dateArray = date.split("/");
                                date = dateArray[2] + "-" + dateArray[1] + "-" + dateArray[0];
                                var div = document.createElement("div");
                                div.innerHTML = "Version: " + rel.version + ", Date: " + date;
                                arow.appendChild(div);

                                var selectButton = document.createElement("button");
                                selectButton.innerHTML = "select";
                                selectButton.style.cursor = "pointer";
                                selectButton.setAttribute("class","addButton");
                                selectButton.setAttribute("data", rel.version + "*" + date + "*" + rel.downloadUrl + "*" + rel.license);
                                arow.appendChild(selectButton);

                                table.appendChild(arow);
                            }
                        }
                        _this.modal.refresh(table);
                        table.addEventListener("click", function (e) {
                            if (e.target.nodeName === "BUTTON") {
                                _this.modal.close();
                                var data = e.target.getAttribute("data").split("*");
                                document.getElementById("comp_version").value = data[0];
                                document.getElementById("releaseDate").value = data[1];
                                document.getElementById("downloadUrl").value = data[2];
                                document.getElementById("MAIN_LICENSE_IDSDisplay").value = data[3];
                                document.getElementById("MAIN_LICENSE_IDS").value = data[3];
                            }
                        });
                    });
                }
            };

            releaseButton.onclick = function() {
                if (!_this.modal.isOpened()) {
                    _this.modal.open();
                    var vendor = document.getElementById("VENDOR_IDDisplay").value;
                    var name = document.querySelectorAll("[name='_components_WAR_sw360portlet_NAME']")[0].value;
                    if (name.indexOf("/") > 0) {
                        var vendorName = name.split("/");
                        vendor = vendorName[0];
                        name = vendorName[1];
                    }

                    _this._autocomplete_repo("name", name, 100, function (repoList) {
                        var filteredRepoList = [];
                        if (vendor && vendor.length > 0) {
                            vendor = vendor.toLowerCase();
                            for (var i = 0; i < repoList.length; i++) {
                                if (repoList[i].owner.toLowerCase().indexOf(vendor) === 0) {
                                    filteredRepoList.push(repoList[i]);
                                }
                            }
                        } else {
                            filteredRepoList = repoList;
                        }

                        if (filteredRepoList.length > 0) {
                            var table = document.createElement("div");
                            table.style.display = "flex";
                            table.style.flexDirection = "column";
                            table.style.height = "auto";
                            table.style.width = "80%";

                            for (var i = 0; i < filteredRepoList.length; i++) {
                                var repo = filteredRepoList[i];

                                var row = document.createElement("div");
                                row.style.display = "flex";
                                row.style.flexDirection = "row";
                                row.style.justifyContent = "space-between";
                                row.style.padding = "5px";
                                row.style.borderBottom = "1px solid rgb(197, 197, 197)";

                                var div = document.createElement("div");
                                var p = document.createElement("p");
                                p.innerHTML = "Vendor: " + repo.owner + ", Name: " + repo.name;
                                div.appendChild(p);
                                row.appendChild(div);

                                var selectButton = document.createElement("button");
                                selectButton.innerHTML = "select";
                                selectButton.style.cursor = "pointer";
                                selectButton.setAttribute("class","addButton");
                                selectButton.setAttribute("data",repo.id);
                                row.appendChild(selectButton);

                                table.appendChild(row);
                            }
                            _this.modal.refresh(table);
                            table.addEventListener("click", selectRepoFromList);
                        } else {
                            var info = document.createElement("span");
                            info.style.display = "flex";
                            info.innerHTML = "NOT SUPPORTED";
                            _this.modal.refresh(info);
                        }
                    });
                }
            };
        };

        this.activateAutoFill = function() {
            _this._install_form_element();
            _this._install_autocomplete_box();
            _this._listen_autocomplete();
        };

        this.activateIndexes = function(tableID) {
            var requestData = [];
            _this.indexTable = document.getElementById(tableID);

            for (var i = 0; i < dataTable.length; i ++) {
                var swComponent = dataTable[i];
                requestData.push({
                    id: swComponent.DT_RowId,
                    vendor: swComponent.vndrs,
                    name: swComponent.name
                });
            }

            _this._fetch_composite(requestData, function (responseData) {
                for (var i = 0; i < responseData.length; i ++) {
                    var data = responseData[i];
                    _this.indexData[data.id] = data;
                }
            });
        };

        this.updateIndexes = function () {
            if (_this.indexDone) {
                return;
            }

            var updateAction = function () {
                if (Object.keys(_this.indexData).length === 0) {
                    if (_this.timeout) {
                        clearTimeout(_this.timeout);
                    }
                    _this.timeout = setTimeout(updateAction,  _this.interval);
                    return;
                }
                var trs = _this.indexTable.getElementsByTagName("tbody")[0].getElementsByTagName("tr");
                for (var i = 0; i < trs.length; i++) {
                    var tr = trs[i];
                    var elementData = _this.indexData[tr.getAttribute("id")];
                    var composite = elementData.compositeIndex;
                    if (composite) {
                        var box = document.createElement("div");
                        box.style.display = "flex";
                        box.style.width = "auto";
                        box.style.flexDirection = "row";

                        var img = document.createElement("img");
                        img.style.display = "flex";
                        img.style.width = "35px";
                        img.style.height = "35px";
                        img.style.borderRadius = "3px";
                        img.style.margin = "10px 10px 10px 0";
                        img.setAttribute("src", elementData.logo);
                        box.appendChild(img);

                        var pR = document.createElement("div");
                        pR.style.margin = "5px 10px 10px 0";
                        pR.style.display = "flex";
                        pR.style.flexDirection = "column";
                        pR.innerHTML = "<div>rate</div><div>" + elementData.rate + "</div>";
                        box.appendChild(pR);

                        var pI = document.createElement("div");
                        pI.style.margin = "5px 10px 10px 0";
                        pI.style.color = "rgb(1, 219, 187)";
                        pI.style.display = "flex";
                        pI.style.flexDirection = "column";
                        pI.innerHTML = "<div>interest</div><div>" + composite.interestPercent + "</div>";
                        box.appendChild(pI);

                        var pA = document.createElement("div");
                        pA.style.margin = "5px 10px 10px 0";
                        pA.style.color = "rgb(237, 81, 56)";
                        pA.style.display = "flex";
                        pA.style.flexDirection = "column";
                        pA.innerHTML = "<div>activity</div><div>" + composite.activityPercent + "</div>";
                        box.appendChild(pA);

                        var pH = document.createElement("div");
                        pH.style.margin = "5px 10px 10px 0";
                        pH.style.color = "rgb(7, 180, 0)";
                        pH.style.display = "flex";
                        pH.style.flexDirection = "column";
                        pH.innerHTML = "<div>health</div><div>" + composite.healthPercent + "</div>";
                        box.appendChild(pH);

                        var info = document.createElement("img");
                        info.className = "codeScoopInfo";
                        box.style.cursor = "pointer";
                        info.style.margin = "10px 10px 10px 0";
                        info.style.width = "15px";
                        info.style.height = "15px";
                        info.setAttribute("src", "/sw360-portlet/images/ic_info.png");
                        box.appendChild(info);

                        tr.getElementsByTagName("td")[1].appendChild(box);
                    }
                }
                _this.indexDone = true;

                var indexInfoMessage =
                    "<p><strong>Codescoop data calculation</strong></p>" +
                    "<p><strong>Interest:</strong> Admiration and re-use by users, measured as weighted view of Stars, Forks and Repo's age.</p>" +
                    "<p><strong>Activity:</strong> Contributor efforts and frequency, measured as weighted view of code development (PRs, releases)</p>" +
                    "<p><strong>Health:</strong> Project maintenance consistency, weighted view of issues handling and commits</p>" +
                    "<p><strong>Rate:</strong> based on result of all indexes and evaluated as a percentage from all components data that we have</p>";

                var tableBody = _this.indexTable.getElementsByTagName("tbody")[0];
                tableBody.addEventListener("click", function (e) {
                    if (e.target.className === "codeScoopInfo") {
                        var content = document.createElement("div");
                        content.style.width = "80%";
                        content.innerHTML = indexInfoMessage;
                        _this.modal.open(content);
                    }
                });
            };

            if (_this.timeout) {
                clearTimeout(_this.timeout);
            }
            _this.timeout = setTimeout(updateAction, _this.interval);
        };

        this.activateRelease = function() {
            _this._init_release_button();
        };
    }

    return codeScoop;
});