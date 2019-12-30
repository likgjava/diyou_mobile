var themes_dir = '/wapassets/default/js';
diyou.config({
    alias: {
        'jquery': themes_dir + '/plugins/jquery-1.8.2.min', 
        'angular': themes_dir + '/plugins/angular.min',
        'w5cValidator': themes_dir + '/plugins/w5cValidator',
        'ngCookie': themes_dir + '/plugins/angular-cookies.min',
        'getUrl': themes_dir + '/services/get-services',
        'dylayer': themes_dir + '/dyplugins/dylayer.m',   //帝友弹窗插件
        'ngDialog': themes_dir + '/plugins/ngDialog.min',       //弹窗插件
        'filters': themes_dir + '/filters/filters',
        'dyDirective': themes_dir + '/directives/dyDirective',
        'indexController': themes_dir + '/controllers/indexController',
        'userController': themes_dir + '/controllers/userController',
        'systemController': themes_dir + '/controllers/systemController',
        'slide': themes_dir + '/plugins/touchslide',
        'dropload': themes_dir + '/plugins/dropload.min',
        'common': themes_dir + '/common',        
        'tool': themes_dir + '/tool',
        'submitForm': themes_dir + '/plugins/jquery.form'
    },
    preload:["jquery","angular"]
});
