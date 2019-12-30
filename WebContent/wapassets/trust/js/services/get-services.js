define(function(require, exports, module) {
    function isEmpty(obj) {
        for (var name in obj) {
            return true;
        }
        return false;
    }
    angular.module('get.services', [])
        .run(["$rootScope", function($rootScope) {
            $rootScope.updateDisabledSub = function(formName, status) { //释放禁用提交按钮
                if (formName) {
                    $rootScope.Is_submitted[formName] = status;
                    if (!status) {
                        $rootScope.$apply();
                    }
                }
            }
        }])
        .factory('postUrl', ['$http', function($http) {
            var doRequest = function(url, data) {
                return $http({
                    method: 'post',
                    url: url,
                    data: data ? $.param(data) : '',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    }
                });
            }
            return {
                events: function(url, data) {
                    return doRequest(url, data);
                }
            }
        }])
        .factory('trustSkip', ['$rootScope', '$http', '$injector', function($rootScope, $http, $injector) {
            return {
                events: function(url, data, obj) {
                    var postUrl =$injector.get('postUrl'),
                        $layer = $injector.get('$dylayer');
                    postUrl.events(url,data).success(function(_data){
                        if (_data.status == 200) {
//                            $layer.open({   
//                                msg:"去开通",
//                                yes:function(){
//                                    $("#diyouTrust").submit();
//                                }
//                            });
                            $('body').append(_data.description.form);
                        } else {
                            $layer.alert(_data.description,1000);
                        }
                    });
                }
            }
        }])
        
});
