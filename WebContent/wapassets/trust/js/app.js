/*--------------------------------------------------------------------
   *author:james;
   *date:2015-04-22;
   *description:v5手机wap端;
   *
----------------------------------------------------------------------*/
define(function(require, exports, module) {
	require('cookie');
	require('w5cValidator');
	require('dylayer');
	require('filters');
	require('indexController');
	require('userController');
	require('systemController');
	require('ngDialog');
	angular.module('ie7support', []).config(function($sceProvider) {
        $sceProvider.enabled(false);
    });
    var app = angular.module('app', ['dyApp','dyUser','dySystem','filters','ie7support']);
});
