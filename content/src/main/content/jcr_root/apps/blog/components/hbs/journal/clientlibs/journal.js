/*
 *
 * ADOBE CONFIDENTIAL
 * __________________
 *
 *  Copyright 2012 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 */
(function($CQ, _, Backbone, SCF) {
    "use strict";
    var Journal = SCF.Forum.extend({
        modelName: "JournalModel",
        relationships: {
            "items": {
                collection: "BlogTopicList",
                model: "BlogTopicModel"
            }
        },
        createOperation: "social:createJournal",
        events: {
            ADD: "blogtopic:added",
            ADD_ERROR: "blogtopic:adderror"
        },
        shouldCommentBeAddedToList: function(topic) {
            var listType = this.view.listTypes.PUBLISHED;
            var topicType = this.view.listTypes.PUBLISHED;
            if (topic) {
                if (topic.get("draft")) {
                    var topicPublishDate = topic.get("publishDate");
                    if (topicPublishDate) {
                        var publishDate = new Date(topicPublishDate);
                        if (publishDate > new Date()) {
                            topicType = this.view.listTypes.SCHEDULED_LATER;
                        } else {
                            topicType = this.view.listTypes.DRAFTS;
                        }
                    } else {
                        topicType = this.view.listTypes.DRAFTS;
                    }
                }
            }
            var modelUrl = this.url;
            if (typeof modelUrl == "string" && modelUrl.indexOf("filter") > -1) {
                if (modelUrl.indexOf(this.view.filterURLParam.SCHEDULED_LATER_URL_FILTER) > 0) {
                    listType = this.view.listTypes.SCHEDULED_LATER;
                } else {
                    listType = this.view.listTypes.DRAFTS;
                }
            }
            if (topicType == listType) {
                return true;
            } else {
                this.view.update();
                return false;
            }
        }
    });
    var JournalView = SCF.ForumView.extend({
        viewName: "Journal",
        COMMUNITY_FUNCTION: "Blog",
        eventBinded: false,
        filterURLParam: {
            DRAFT_URL_FILTER: "?filter=isDraft%20eq%20%27true%27&filter=publishDate%20eq%20null",
            SCHEDULED_LATER_URL_FILTER: "?filter=isDraft%20eq%20%27true%27&filter=publishDate%20ne%20null"
        },
        listTypes: {
            PUBLISHED: "published",
            SCHEDULED_LATER: "scheduledLater",
            DRAFTS: "drafts"
        },
        init: function() {
            SCF.ForumView.prototype.init.apply(this);
            var resourceType = this.model.get("resourceType");
            var templateName = "journallists";
            this.listenTo(this.model, 'change:composedForValid', this.composedForChanged);
            this.listTemplateC = SCF.findTemplate(this.model.id, templateName, resourceType);
            this.listenTo(this.model, 'model:loaded', function() {
                this._modelReady = true;
                var modelUrl = this.model.url;
                if (modelUrl.indexOf("filter") > -1) {
                    this.renderWithTemplate();
                } else {
                    this.update();
                }
            });
        },
        composedForChanged: function() {
            // Flag in model changed to indicate that a chosen username to "Compose om behalf of a user is invalid.
            if (this.model.get("composedForValid")) {
                this.$el.find(".scf-js-userfilter").removeClass("scf-error");
                this.$el.find(".scf-js-invalid-user").addClass("scf-is-hidden").removeClass("scf-js-error-message");
                this.$el.find(".scf-js-publish-btn").prop("disabled", false);
            } else {
                this.$el.find(".scf-js-userfilter").addClass("scf-error");
                this.$el.find(".scf-js-invalid-user").addClass("scf-js-error-message").removeClass("scf-is-hidden");
                this.$el.find(".scf-js-publish-btn").prop("disabled", true);
            }
        },
        update: function() {
            var modelUrl = this.model.url;
            if (typeof modelUrl == "string" && modelUrl.indexOf("filter") > -1) {
                var composer = this.$el.find(".scf-js-composer-block");
                if (composer.is(":visible")) {
                    SCF.ForumView.prototype.toggleComposer.apply(this, []);

                }
                this.renderWithTemplate();
                if (!_.isNull(this.model.url) && this.model.url.indexOf(this.filterURLParam.SCHEDULED_LATER_URL_FILTER) >
                    0) {
                    this.activateTabs(".scf-js-laterPosts", "#scf-js-laterPosts");
                } else {
                    this.activateTabs(".scf-js-draftPosts", "#scf-js-draftPosts");
                }
                this.$el.find(".scf-js-journal-tab").removeClass("scf-is-hidden");
            } else {
                SCF.ForumView.prototype.update.apply(this);
            }

        },
        activateTabs: function(activeTabSelector, tabId) {
            this.$el.find(activeTabSelector).parent().addClass("active");
            this.$el.find(tabId).addClass("active");
        },
        renderWithTemplate: function() {
            //this.template = this.listTemplateC;
            this.$el.find(".tab-pane").empty();
            var element = $CQ(this.listTemplateC(this.getContextForTemplate(), {
                data: {
                    parentView: this
                }
            }));
            if (!_.isNull(this.model.url) && this.model.url.indexOf(this.filterURLParam.SCHEDULED_LATER_URL_FILTER) >
                0)
                this.$el.find("#scf-js-laterPosts").empty().append(element);
            else
                this.$el.find("#scf-js-draftPosts").empty().append(element);
            var that = this;
            _.each(this._childViews, function(child) {
                that.renderChildView(child);
            });

            var finishRendering = _.bind(function() {
                this.bindView();
                this._rendered = true;
                if (this.afterRender) {
                    this.afterRender();
                }
                this.trigger("view:rendered", {
                    view: this
                });

            }, this);
            //wait for children to finish rendering and then complete binding the view
            $CQ.when(this._renderedChildren).done(finishRendering);
            this.$el.find("li.scf-journal-tab").removeClass("active");
            this.$el.find(".tab-pane").removeClass("active");
            return this;
        },
        toggleComposer: function(e) {
            //Toggle Pagination Block
            //Toggle Items
            this.$el.find(".scf-js-journal-tab").toggleClass("scf-is-hidden");
            this.$el.find(".scf-topic-list").toggleClass("scf-is-hidden");
            this.$el.find(".scf-pages").toggleClass("scf-is-hidden");
            var composer = this.$el.find(".scf-js-composer-block");
            if (composer.hasClass("scf-is-collapsed")) {
                this.eventBinded = false;
            }
            //Bind datepicker and time autocomplete
            if (!this.eventBinded) {
                this.bindDatePicker(e);
                this.eventBinded = true;
            }
            //Resize CKEditor
            SCF.ForumView.prototype.toggleComposer.apply(this, [e]);
        },
        bindDatePicker: function(e) {
            var addOptions = function(element, minOptionValue, maxOptionValue) {
                if (element.has('option').length <= 0) {
                    var that = this;
                    $CQ.each(_.range(minOptionValue, maxOptionValue), function(key, value) {
                        value = value < 10 ? ("0" + value) : value;
                        value = CQ.I18n.get(value);
                        element.append($("<option/>", {
                            value: value,
                            text: value
                        }));
                    });
                }
            };


            // bind drop down button group
            this.$el.find(".scf-js-pubish-type .dropdown-menu li a").click(function() {
                $(".scf-js-pubish-type .btn:first-child").html(
                    '<span id="button_label">' + $(this).text() +
                    '</span> <span class="caret"></span>');
                $(".scf-js-pubish-type .btn:first-child").val($(this).text());
            });

            // bind date picker
            this.$el.find(".scf-js-event-basics-start-input").datepicker({
                changeMonth: true,
                numberOfMonths: 2,
                setDate: "0",
                beforeShow: function(input, inst) {
                    $('#ui-datepicker-div').wrap(
                        "<div class='scf-datepicker'></div>");
                },
                onClose: function(selectedDate) {
                    $CQ(".scf-event-basics-start-input").datepicker("option", "minDate",
                        selectedDate);
                }
            }).datepicker("setDate", new Date());

            //add options to hour and minute time picker to keep markup simple
            addOptions(this.$el.find(".scf-js-event-basics-start-hour"), 1, 13);
            addOptions(this.$el.find(".scf-js-event-basics-start-min"), 0, 60);

            //set default date
            var date = new Date();
            var hours = date.getHours();
            var minutes = date.getMinutes();
            var ampm = hours >= 12 ? "PM" : "AM";
            hours = hours > 12 ? hours - 12 : hours;
            hours = hours < 10 ? ("0" + hours) : hours;
            minutes = minutes < 10 ? ("0" + minutes) : minutes;
            this.$el.find(".scf-js-event-basics-start-hour").val(hours.toString());
            this.$el.find(".scf-js-event-basics-start-min").val(minutes.toString());
            this.$el.find(".scf-js-event-basics-start-time-ampm").val(ampm);
        },
        showDraftOption: function(e) {
            this.$el.find(".scf-js-publish-time-input").addClass("scf-is-hidden");
            this.$el.find(".scf-js-save-draft-btn").show();
            this.$el.find(".scf-js-publish-btn").hide();
        },
        showPublishOption: function(e) {
            this.$el.find(".scf-js-publish-time-input").removeClass("scf-is-hidden");
            this.$el.find(".scf-js-save-draft-btn").hide();
            this.$el.find(".scf-js-publish-btn").show();
        },
        showImmediatelyOption: function(e) {
            this.$el.find(".scf-js-publish-time-input").addClass("scf-is-hidden");
            this.$el.find(".scf-js-save-draft-btn").hide();
            this.$el.find(".scf-js-publish-btn").show();
        },
        getOtherProperties: function() {
            var subject = this.getField('subject').trim();
            var publishMode = $(this.$el.find(".scf-js-pubish-type > button > span")).text();
            var draftMode = false;
            var publishDate = null;
            var isScheduled = false;
            if (!_.isEmpty(publishMode) && publishMode == $(this.$el.find(
                    ".scf-js-pubish-type > ul > li > a")[1])
                .text()) {
                draftMode = true;
            } else if (!_.isEmpty(publishMode) && publishMode == $(this.$el.find(
                    ".scf-js-pubish-type > ul > li > a")[2]).text()) {
                draftMode = true;
                isScheduled = true;
                publishDate = getDateTime(this.$el.find(".scf-js-event-basics-start-input").val(),
                    this.$el.find(".scf-js-event-basics-start-hour").val(), this.$el.find(
                        ".scf-js-event-basics-start-min").val(), this.$el.find(".scf-js-event-basics-start-time-ampm").val());
            }
            var props = {
                'subject': subject,
                'isDraft': draftMode
            };
            if (isScheduled) {
                props["isScheduled"] = true;
                props["publishDate"] = publishDate;
            }
            if (this.model.getConfigValue("usingPrivilegedUsers")) {
                var composedFor = this.getField("composedFor");
                if (!_.isEmpty(composedFor)) {
                    props.composedFor = composedFor;
                }
            }
            this.eventBinded = false;
            return props;
        },
        fetchDrafts: function() {
            this.switchView(this.filterURLParam.DRAFT_URL_FILTER, ".scf-js-draftPosts",
                "#scf-js-draftPosts");
        },
        fetchLaterPosts: function() {
            this.switchView(this.filterURLParam.SCHEDULED_LATER_URL_FILTER,
                ".scf-js-laterPosts",
                "#scf-js-laterPosts");
        },
        fetchAllPosts: function() {
            this.switchView("", ".scf-js-allPosts", "#scf-js-allPosts");
        },
        switchView: function(filter, activeTabSelector, tabId) {
            this.model.url = this.model.id + SCF.constants.URL_EXT + filter;
            var that = this;
            SCF.log.debug("switchView:" + this.model.url);
            this.model.reload({
                success: function() {
                    that.activateTabs(activeTabSelector, tabId);
                },
                error: function() {
                    SCF.log.error("Error reloading model");
                }
            });
        }
    });

    var getDateTime = function(date, hours, minutes, ampm) {
        if (!date || !hours || !minutes) {
            return undefined;
        }
        try {
            if (ampm === "PM" && hours !== "12") {
                hours = parseInt(hours);
                hours = hours + 12;
            }
            var time = ((parseInt(hours) * 60) + parseInt(minutes)) * 60000;
            var dateObj = Date.parse(date) + time;
            return (new Date(dateObj).toISOString());
        } catch (e) {
            return NaN;
        }
    };

    var BlogPost = SCF.Post.extend({
        modelName: "BlogPostModel",
        DELETE_OPERATION: "social:deleteJournalComment",
        UPDATE_OPERATION: "social:updateJournal",
        CREATE_OPERATION: "social:createJournal",
        relationships: {
            "items": {
                collection: "BlogPostList",
                model: "BlogPostModel"
            },
            "votes": {
                model: "VotingModel"
            }
        }
    });

    var BlogPostView = SCF.PostView.extend({
        viewName: "BlogPost"
    });

    var BlogTopic = SCF.Topic.extend({
        modelName: "BlogTopicModel",
        DELETE_OPERATION: "social:deleteJournalComment",
        UPDATE_OPERATION: "social:updateJournal",
        CREATE_OPERATION: "social:createJournal",
        relationships: {
            "items": {
                collection: "BlogTopicList",
                model: "BlogTopicModel"
            },
            "votes": {
                model: "VotingModel"
            }
        },
        getCustomProperties: function() {
            var customData = {
                subject: this.get("subject")
            };
            if (this.has("isDraft")) {
                customData.isDraft = this.get("isDraft");
                var publishDate = this.get("publishDate");
                if (!_.isEmpty(publishDate)) {
                    customData.publishDate = publishDate;
                    customData.isScheduled = true;
                }
            }
            if (this.getConfigValue("usingPrivilegedUsers")) {
                var composedFor = this.get("composedFor");
                if (!_.isEmpty(composedFor)) {
                    customData.composedFor = composedFor;
                }
            }

            return customData;
        }
    });

    var BlogTopicView = SCF.TopicView.extend({
        viewName: "BlogTopic",
        COMMUNITY_FUNCTION: "Blog",
        eventBinded: false,
        bindDatePicker: JournalView.prototype.bindDatePicker,
        showImmediatelyOption: JournalView.prototype.showImmediatelyOption,
        showPublishOption: JournalView.prototype.showPublishOption,
        showDraftOption: JournalView.prototype.showDraftOption,
        init: function() {
            SCF.TopicView.prototype.init.call(this);
            this.listenTo(this.model, this.model.events.DELETED, this.navigateCancel);
        },
        edit: function(e) {
            this.$el.find(".scf-js-journal-comment-section").toggleClass("scf-is-hidden");
            SCF.TopicView.prototype.edit.call(this, e);
            this.$el.find(".scf-js-topic-details").hide();
            this.$el.find(".scf-js-topic-details-tags-editable").show();
            this.$el.find(".scf-comment-toolbar .scf-comment-edit").hide();

            var subject = this.model.get('subject');
            this.setField("editSubject", subject);
            this.focus("editSubject");
            if (!this.eventBinded) {
                this.bindDatePicker(e);
                this.eventBinded = true;
            }
        },
        afterRender: function() {
            SCF.TopicView.prototype.afterRender.call(this);
            if (!this.eventBinded) {
                this.bindDatePicker();
                this.eventBinded = true;
            }

            // set publish options
            var dropDownButton = this.$el.find(".scf-js-pubish-type > button > span")[0];
            // return if dropdown is not visible
            if (dropDownButton === undefined) return;

            var isDraft = "false";
            if (this.model.get("properties") && this.model.get("properties").isDraft) {
                isDraft = this.model.get("properties").isDraft;
            }
            var publishDate;
            if (this.model.get("publishDate")) {
                publishDate = this.model.get("publishDate");
            }
            if (isDraft == "true" && publishDate != null) {
                $(dropDownButton).text(CQ.I18n.getMessage("At scheduled date and time"));
                var date = new Date(publishDate);
                var hours = date.getHours();
                var minutes = date.getMinutes();
                this.$el.find(".scf-js-event-basics-start-input").datepicker("setDate", date);
                var ampm = hours >= 12 ? "PM" : "AM";
                hours = hours > 12 ? hours - 12 : hours;
                hours = hours < 10 ? ("0" + hours) : hours;
                minutes = minutes < 10 ? ("0" + minutes) : minutes;
                hours = hours.toString();
                minutes = minutes.toString();
                this.$el.find(".scf-js-event-basics-start-hour").val(hours);
                this.$el.find(".scf-js-event-basics-start-min").val(minutes);
                this.$el.find(".scf-js-event-basics-start-time-ampm").val(ampm);
                this.showPublishOption();
            } else if (isDraft == "true") {
                $(dropDownButton).text(CQ.I18n.getMessage("Draft"));
                this.showDraftOption();
            }
        },
        navigateCancel: function(e) {
            window.location.href = this.model.get("pageInfo").basePageURL + ".html";
        },
        cancel: function(e) {
            this.$el.find(".scf-js-journal-comment-section").toggleClass("scf-is-hidden");
            SCF.TopicView.prototype.cancel.call(this, e);
            this.$el.find(".scf-js-topic-details").show();
            this.$el.find(".scf-js-topic-details-tags-editable").hide();
            this.$el.find(".scf-comment-toolbar .scf-comment-edit").show();
        },
        getOtherProperties: function() {
            var subject = this.getField("editSubject").trim();
            var tags = this.getField("editTags");
            var props = {
                'subject': subject,
                'tags': tags
            };
            var publishMode = $(this.$el.find(".scf-js-pubish-type > button > span")).text();
            var publishDate = null;
            if (!_.isEmpty(publishMode) && publishMode == $(this.$el.find(
                    ".scf-js-pubish-type > ul > li > a")[1])
                .text()) {
                props.isDraft = true;
            } else if (!_.isEmpty(publishMode) && publishMode == $(this.$el.find(
                    ".scf-js-pubish-type > ul > li > a")[2]).text()) {
                props.isDraft = true;
                props.isScheduled = true;
                props.publishDate = getDateTime(this.$el.find(
                        ".scf-js-event-basics-start-input").val(),
                    this.$el.find(".scf-js-event-basics-start-hour").val(), this.$el.find(
                        ".scf-js-event-basics-start-min").val(), this.$el.find(".scf-js-event-basics-start-time-ampm").val());
            } else {
                props.isDraft = false;
            }
            if (this.model.getConfigValue("usingPrivilegedUsers")) {
                var composedFor = this.getField("composedFor");
                if (!_.isEmpty(composedFor)) {
                    props.composedFor = composedFor;
                }
            }
            this.eventBinded = false;
            return props;
        },
        deleteArticle: function(e) {
            e.stopPropagation();
            this.unbindDataFields();
            this.model.remove();
            //window.location.href = this.model.get("pageInfo").basePageURL  + ".html";
        },
        toggleComposerCollapse: function(e) {
            this.$el.find(".scf-js-composer-block").toggleClass("scf-is-collapsed");
            this.focus("replyMessage");
        }
    });

    var BlogTopicList = Backbone.Collection.extend({
        collectionName: "BlogTopicList"
    });
    var BlogPostList = Backbone.Collection.extend({
        collectionName: "BlogPostList"
    });

    SCF.BlogPost = BlogPost;
    SCF.BlogTopic = BlogTopic;
    SCF.Journal = Journal;
    SCF.BlogTopicView = BlogTopicView;
    SCF.BlogPostView = BlogPostView;
    SCF.JournalView = JournalView;
    SCF.BlogTopicList = BlogTopicList;
    SCF.BlogPostList = BlogPostList;
    SCF.registerComponent('blog/components/hbs/comment', SCF.BlogPost, SCF.BlogPostView);
    SCF.registerComponent('blog/components/hbs/entry_topic', SCF.BlogTopic, SCF.BlogTopicView);
    SCF.registerComponent('blog/components/hbs/journal', SCF.Journal, SCF.JournalView);
})($CQ, _, Backbone, SCF);
