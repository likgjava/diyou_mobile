/*--------------------------------------------------------------------
   *author:ray;
   *date:2015-07-9;
   *description:v5手机wap端;
   *
----------------------------------------------------------------------*/
define(function(require, exports, module) {

    //首页
    exports.slide = function(){
        //banner
        require('slide');
        TouchSlide({
            slideCell:"#banner",
            titCell:".hd ul", //开启自动分页 autoPage:true ，此时设置 titCell 为导航元素包裹层
            mainCell:".bd ul",
            effect:"left",
            autoPlay:true,//自动播放
            autoPage:true //自动分页
        });

        var banner = $('#banner');
        var height = banner.find('img').eq(0).height();
        banner.height(height);
    }

    //进度
    exports.progbar = function(){
      setTimeout(function(){
        $('.progbar').each(function(){
          var ts = $(this);
          var rount = ts.find('.prog-rount');
          var rount2 = ts.find('.prog-rount2');
          var text = ts.find('.prog-text');
          var num = text.attr('data');//parseInt(text.html());
          var val = text.attr('rel');

          if(num != 100){
            val = num+'%';
          }

          text.html(val);

          if(num <= 50){
            rount.css({'-webkit-transform': 'rotate('+3.6*num+'deg)'});
            rount2.hide();
          }else{

            rount.css({'-webkit-transform': 'rotate(180deg)'});
            rount2.css({'-webkit-transform': 'rotate('+3.6*(num-50)+'deg)'});
            rount2.show();
          }
        })
      },500)

    }

    //计算器
    exports.counter = function(){
      $('.counter').on('click', function(){
        $('body').addClass('counter-cover');
      })
      $('.ui-counter .exit').on('click', function(){
        $('body').removeClass('counter-cover');
      })
    }

    //联系客服
    exports.contactsShow = function(){
      $('body').addClass('hotline-cover');
    }
    exports.contactsHide = function(){
      $('body').removeClass('hotline-cover');
    }

    //投资详情 切换
    exports.tabs = function(){
      $('#loanTabs .hd li').on('click', function(){
        var i = $(this).index();
        $(this).addClass('on').siblings().removeClass('on')
        $('#loanTabs .bd .bd-item').eq(i).addClass('on').siblings().removeClass('on');
      })
    }

    //投标页面
    exports.tenderLoan = function(){

      $('#tenderPay .back, #tenderPay .btn-cancel, .overlay').on('click', function(){
        $('body').removeClass('cover');
      })
    }

    //隐藏输入交易密码
    exports.hideLoan = function(){
      $('body').removeClass('cover');
      //$('#tenderPay').removeClass('inup').prev('.overlay').hide();
    }


    //显示图表
    exports.showEcharts = function() {
        require('echarts');
        // 基于准备好的dom，初始化echarts实例
        var myChart = echarts.init(document.getElementById('main'));

        // 指定图表的配置项和数据
        var option = {
            tooltip: {
                trigger: 'item',
                formatter: "{a} <br/>{b}: {c} ({d}%)"
            },
            color:['#317ef3', '#83c4f3','#b2e0ef','#d6f0e0', '#f3d887', '#ff7f2e'] ,
            legend: {
                orient: 'vertical',
                x: '220px',
                y:'25px',
                itemGap: 5,
                align: 'left',
                data:['车商贷', '车主贷', '现金贷', '房抵贷', 'X计划', 'PP项目']
            },
            series: [
                {
                    name:'比例',
                    type:'pie',
                    radius: ['50%', '70%'],
                    avoidLabelOverlap: false,
                    label: {
                        normal: {
                            show: false,
                            position: 'center',
                            x:'20',
                        }
                    },
                    labelLine: {
                        normal: {
                            show: false
                        }
                    },
                    data:[
                        {value:335, name:'车商贷'},
                        {value:310, name:'车主贷'},
                        {value:234, name:'现金贷'},
                        {value:135, name:'房抵贷'},
                        {value:1548, name:'X计划'},
                        {value:666, name:'PP项目'}
                    ]
                }
            ]
        };
        window.onresize = function () {
            myChart.resize(); //使第一个图表适应
        }
        // 使用刚指定的配置项和数据显示图表。
        myChart.setOption(option);
        };
});
