angular.module("dylayer.m", ["ng"])
    .provider('$dylayer', [function () {
    	var $injector,
            $scope,
            $http,
            $q,
            $timeout,
            _this = this;


        /**
         * Setup the provider
         * @param injector
         */
        var setup = function(injector) {
            $injector = injector;
            $scope = $injector.get('$rootScope');
            $http = $injector.get('$http');
            $q = $injector.get('$q');
            $timeout = $injector.get('$timeout');
        };
        //创建弹窗构造函数
    	var dylayerFn = function(){
            var that = this /*, 
                conf = params*/ ;
            //that.config = angular.extend({},that.config,conf);
            //that.dyCreatDislog(params);    
    	}
    	dylayerFn.prototype = {
            //弹窗初始化配置参数
            config: {
                type:0,  //默认类型，信息弹窗
                title:"",
                page:{
                  url:""
                },
                btn: ['确认', '取消'],
                end: function(){}
            },
            alert: function(msg, time, end){
                var type = typeof time;
                if(type === "function"){
                    end = time;                    
                }
                if(type !== "number"){
                    time = 2000;                    
                } 

                var that = this,
                    conf = {
                        type:0,
                        msg:msg,
                        time:time,
                        end:end
                    }
                that.config = angular.extend({},that.config,conf);
                that.dyCreatDislog(that.config);
            },
            confirm:function(options, yes, no){

                var type = typeof options === 'object';
                if (type) {
                    options.type = 1;
                }
                var that = this,
                    conf = {
                        type: 1,
                        msg: options,                        
                        yes: yes,
                        no: no
                    }
                that.config = angular.extend({},that.config,type ? options : conf);
                that.dyCreatDislog();
            },
            close:function(callback){
                $(".layer-overlay").remove();
                $(".layer").remove();
                setTimeout(function(){
                    callback()
                },500);
            },
            page:function(options,yes,no){
                var type = typeof options === 'object';
                if (type) {
                    options.type = 2;
                }
                var that = this,
                    conf = {
                        type: 2,
                        page: options
                    }
                that.config = angular.extend({},that.config,type ? options : conf);
                that.dyCreatDislog();
            },
            //创建弹窗
            dyCreatDislog : function(){
                var that = this,
                    config = that.config,
                    layer;
                switch(config.type){

                    case 0 :
                        layer = $('<div class="layer-overlay"></div>'+
                                '<div class="layer">'+
                                '<div class="layer-inner">'+
                                '<div class="layer-alert">'+config.msg+
                                '</div></div></div>');
                        layer.appendTo('body');
                        setTimeout(function () {
                            layer.remove();
                            if( typeof config.end === 'function' ){
                                config.end();
                            }                            
                        }, config.time);
                        break;

                    case 1 :
                        layer = $('<div class="layer-overlay"></div>'+
                                '<div class="layer">'+
                                '<div class="layer-inner">'+
                                '<div class="layer-confirm">'+
                                '<div class="layer-cont">'+config.msg+'</div>'+
                                '<div class="layer-btns">'+
                                '<span class="btn btn-no">'+config.btn[1]+'</span>'+
                                '<span class="btn btn-yes">'+config.btn[0]+'</span>'+
                                '</div></div></div></div>');
                        layer.appendTo('body');
                        layer.find('.btn-yes').on('click', function(){
                            if( typeof config.yes === 'function' ){
                                config.yes();
                            }  
                            layer.remove();
                        })
                        layer.find('.btn-no').on('click', function(){
                            if( typeof config.no === 'function' ){
                                config.no();
                            }  
                            layer.remove();
                        })

                        break;
                    case 2:
                        $.ajax({
                            method:"post",
                            url:config.url,
                            success:function(data){
                                layer = $('<div class="layer-overlay"></div>'+
                                        '<div class="layer">'+
                                        '<div class="layer-page">'+
                                        '<i class="layer-close"></i>'+   
                                        '<div class="layer-main">'+
                                        '<div class="layer-cont">'+data+
                                        '</div></div></div></div>');
                                layer.appendTo('body');
                                layer.find('.layer-close').on('click', function(){
                                    layer.remove();
                                })
                            }
                        });
                        break;
                }
            }
    	}

    	var dylayer = new dylayerFn();
    	this.$get = ['$injector',
            function($injector) {
                setup($injector);
                return dylayer;
            }
        ];
    }])