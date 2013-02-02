var Application = (function() {
	var application = {};
	application.Models = {};
	application.Collections = {};
	application.Views = {};
	application.Router = {};

	application.getTemplate = function(template) {
		return $.ajax({
			type : "GET",
			url : "/backbone/templates/" + template,
			async : false,
			cache : true
		}).responseText;
	},

	toggleMenu = function(item) {
        $('ul.nav > li').removeClass('active');
        $('#'+ item).addClass('active');                
	},
	
	application.Router.RoutesManager = Backbone.Router.extend({
		initialize : function(args) {
			this.videoFolders = args.videoFolders;
			this.audioFolders = args.audioFolders;
			this.pictureFolders = args.pictureFolders;
			this.podcasts = args.podcasts;
			this.defaultView = args.defaultView;
		},
		routes : {
			"videoFolders" : "videoFolders",
			"audioFolders" : "audioFolders",
			"pictureFolders" : "pictureFolders",
			"podcasts" : "podcasts",

			"*path" : "root"
		},

		videoFolders : function() {
			toggleMenu('video_folders_menu');
			this.videoFolders.all().fetch();
		},

		audioFolders : function() {
			toggleMenu('audio_folders_menu');
			this.audioFolders.all().fetch();
		},
		pictureFolders : function() {
			toggleMenu('picture_folders_menu');
			this.pictureFolders.all().fetch();
		},
		podcasts : function() {
			toggleMenu('podcasts_menu');
			this.podcasts.all().fetch();
		},
		root : function() {
			toggleMenu('home_menu');
			this.defaultView.render();
		},
	});

	return application;
}());