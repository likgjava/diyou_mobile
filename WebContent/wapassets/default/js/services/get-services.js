define(function(require, exports, module) {
    angular.module('get.services', [])
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
});
