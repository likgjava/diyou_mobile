/*******************************
  *author:james;
  *name:dyNgExtend;
  *description:帝友v5wap端扩展插件;
  *date:2015-05-13;
********************************/
(function() {
    angular.module('dyNgExtend', ['dyNgExtend.provider', 'dyNgExtend.directive']);
}).call(this);

(function(window,undefined) {
    angular.module('dyNgExtend.provider', [])
        .provider("$ngtest",function(){
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

            var getPhoneCodeFn = function(){
                return new getPhoneCodeFn.prototype.init();
            }
            getPhoneCodeFn.prototype = {
                init:function(){
                    console.log(23);
                },
                test:function(){
                    
                }
            }
            getPhoneCodeFn.prototype.init.prototype = getPhoneCodeFn.prototype;
            if ( typeof window === "object" && typeof window.document === "object" ) {
                window.getPhoneCodeFn = getPhoneCodeFn;
            }
            this.$get = ['$injector',
                function($injector) {
                    setup($injector);
                    return getPhoneCodeFn();
            }];
        })


}).call(this);



(function() {
    angular.module('dyNgExtend.directive', ['dyNgExtend.provider'])
    

}).call(this);

