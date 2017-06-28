(function($CQ, _, Backbone, SCF) {
    "use strict";
    var Idea = SCF.Topic.extend({
        modelName: "IdeaModel",
        setTech: function(tech) {
            var error = _.bind(function(jqxhr, text, error) {
                this.log.error("error setting status " + error);
                this.trigger('idea:statuserror', {
                    'error': error
                });
            }, this);
            var success = _.bind(function(response) {
                this.set(response.response);
                this.trigger('idea:statusset', {
                    model: this
                });
                this.trigger(this.events.UPDATED, {
                    model: this
                });
            }, this);
            var postData = {
                'tech': tech,
                ':operation': 'social:setTechnology'
            };
            $CQ.ajax(SCF.config.urlRoot + this.get('id') + SCF.constants.URL_EXT, {
                dataType: 'json',
                type: 'POST',
                xhrFields: {
                    withCredentials: true
                },
                data: this.addEncoding(postData),
                'success': success,
                'error': error
            });
        },
        addLike:function(likeValue){

            var error = _.bind(function(jqxhr, text, error) {
                this.log.error("error setting status " + error);
                this.trigger('idea:statuserror', {
                    'error': error
                });
            }, this);
            var success = _.bind(function(response) {
                this.set(response.response);
            }, this);

            var postData = {
                'likes' :likeValue,
                ':operation': 'social:addLike'
            };
            $CQ.ajax(SCF.config.urlRoot + this.get('id') + SCF.constants.URL_EXT, {
                async:false,
                dataType: 'json',
                type: 'POST',
                xhrFields: {
                    withCredentials: true
                },
                data: this.addEncoding(postData),
                'success': success,
                'error': error
            });

        }


    });

    var IdeaView = SCF.TopicView.extend({
        viewName: "Idea",
        setTech: function(e){
            var tech = this.getField("tech");
            this.model.setTech(tech.join("-"));
            e.preventDefault();
        },
        addLike:function(event){
             event.preventDefault();
            var likeValue = $(event.target).data('likes');
         	this.model.addLike(likeValue);
            this.render();
        }

    });

    SCF.Idea = Idea;
    SCF.IdeaView = IdeaView;
    SCF.registerComponent('scf/components/ideation/post', SCF.Post, SCF.PostView);
    SCF.registerComponent('scf/components/ideation/topic', SCF.Idea, SCF.IdeaView);
    SCF.registerComponent('scf/components/ideation/forum', SCF.Forum, SCF.ForumView);
})($CQ, _, Backbone, SCF);
