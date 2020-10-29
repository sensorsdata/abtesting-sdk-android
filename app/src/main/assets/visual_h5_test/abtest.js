(function (global, factory) {
  typeof exports === 'object' && typeof module !== 'undefined' ? module.exports = factory() : factory();
}(this, (function () { 'use strict';

  var _ = {};

  var SensorsABTest = {
      sd:null,
      lib_version:'0.1.0',
      para:{},
      default_para:{
        url:'',
        path:'',
        project_key:'',
        timeout_milliseconds:3000,
        update_interval: 10*60*1000,
        collect_bridge_status:true
      },
      value_type_list:['Number','String','Object','Boolean'],
      state:{
        platform:'',
        storage:{
          isSupport:true,
          name:'sawebjssdkabtest'
        }
      },
      bridgeState:'',
      localdata:[],
      localExpID:[],
      //上报试验触发事件，成功命中试验时上报
      trackTestTrigger:function(expObj,otherObj){
        var obj = {
          $abtest_experiment_id: expObj.abtest_experiment_id,
          $abtest_experiment_group_id: expObj.abtest_experiment_group_id
        };
        if(SensorsABTest.para.collect_bridge_status){
          obj['$sdk_bridge_status'] = SensorsABTest.bridgeState;
        }
        var proObj = _.extend(obj,otherObj);
        this.sd.track('$ABTestTrigger',proObj);
      },
      //参数校验
      verifyStore:{
        valueType:function(value,type){
          switch(type){
            case 'Number':
              if(_.isNumber(value)){
                return true;
              }
              break;
            case 'String':
              if(_.isString(value)){
                return true;
              }
              break;
            case 'Object':
              if(_.isObject(value)){
                return true;
              }
              break;
            case 'Boolean':
              if(value === true || value === false){
                return true;
              }
              break;
            default:
              return false;
          }
          return false;
        },
        /**
         * 校验分流API参数是否正确
         *
         * @param {String} name 分流API名称
         * @param {Object} para 分流API参数对象
         * @param {Object} obj  校验规则
         */
        para:function(name,para,obj){
          var result = {
            verify_success:true,
            para:null
          };
          _.each(obj,function(value,prop){
            if(value === 'essential'){
              switch(prop){
                case 'experiment_id':
                  if(!(_.isString(para.experiment_id) && para.experiment_id.length>0)){
                    SensorsABTest.error(name + '方法调用失败，experiment_id参数未正确配置！experiments_id:',para.experiment_id);
                    result.verify_success = false;
                  }
                  break;
                case 'value_type':
                  if(!(_.isString(para.value_type) && _.indexOf(SensorsABTest.value_type_list,para.value_type) !== -1)){
                    SensorsABTest.error(name + '方法调用失败，value_type配置错误',para.value_type);
                    result.verify_success = false;
                  }
                  break;
                case 'default_value':
                  if(typeof(para.default_value) === 'undefined'){
                    SensorsABTest.error(name + '方法调用失败，default_value参数未配置');
                    result.verify_success = false;
                  }else if(!SensorsABTest.verifyStore.valueType(para.default_value,para.value_type)){
                    SensorsABTest.error(name + '方法调用失败，default_value类型必须与value_type一致！',para.default_value,para.value_type);
                    result.verify_success = false;
                  }
                  break;
                default:
                  result.verify_success = false;
                  break;
              }
            }else if(value === 'not_essential'){
              switch(prop){
                case 'callback':
                  if(!_.isFunction(para.callback)){
                    para.callback = function(){};
                    SensorsABTest.error(name + 'callback参数未正确配置');
                  }
                  break;
                case 'timeout_milliseconds':
                  para.timeout_milliseconds = para.timeout_milliseconds || SensorsABTest.para.timeout_milliseconds || SensorsABTest.default_para.timeout_milliseconds;
                  if(!_.isNumber(para.timeout_milliseconds) || (_.isNumber(para.timeout_milliseconds)&& para.timeout_milliseconds<=0)){
                    _.log('timeout_milliseconds 参数错误',para.timeout_milliseconds);
                    para.timeout_milliseconds = SensorsABTest.para.timeout_milliseconds;
                  }
                  if(para.timeout_milliseconds<100){
                    para.timeout_milliseconds=100;
                  }
                  break;
                case 'properties':
                  para.properties = _.isObject(para.properties) ? para.properties : {};
                  break;
              }
            }
          });
          result.para = para;
          return result;
        }
      },
      
      /**
       * 处理服务端response
       * 1、解析数据，服务端失败打印日志
       * 2、处理所有试验，config处理为js_config
       * 3、更新本地数据
       * @param {Object} data responsedata
       */
      dealResponseData:function(data){
        if(_.isObject(data)){
          if(data.status === 'SUCCESS'){
            if(_.isArray(data.results)){
              _.each(data.results,function(item){
                if(_.isObject(item) && _.isObject(item.config)){
                  item.js_config = SensorsABTest.getRelativeValue(item.config.variables,item.config.type);
                }
              });
              this.updateLocalData(data.results);
            }
          }else {
            if(data.status === 'FAILED'){
              _.log('获取试验失败：error_type：'+data.error_type+',error：'+data.error);
            }
          }
        }else {
          _.log('试验数据解析失败，response ：',data);
        }
      },

      /**
       * 将服务端返回的试验variables根据js的type进行转换
       * @param {String} val 服务端返回的 value
       * @param {String} type 服务端返回的 type
       * return {value:123,type:'Number'}
       */
      getRelativeValue:function(val,type){
        var data = {};
        var change_list = {
          INTEGER: function(value){
            var val = parseFloat(value);
            if(!isNaN(val)){
              data.value = val;
              data.type = 'Number';
            }else {
              _.log('服务端数据INTEGER类型解析异常',value);
            }
          },
          STRING: function(value){
            if(_.isString(value)){
              data.value = value;
              data.type = 'String';
            }else {
              _.log('服务端数据STRING类型解析异常',value);
            }
          },
          JSON: function(value){
            var val = JSON.parse(value);
            if(_.isObject(val)){
              data.value = val ;
              data.type = 'Object';
            }else {
              _.log('服务端数据JSON类型解析异常',value);
            }
          },
          BOOLEAN: function(value){
            if(value === 'true'){
              data.value = true;
              data.type = 'Boolean';
            }else if(value === 'false'){
              data.value = false;
              data.type = 'Boolean';
            }else {
              _.log('服务端数据BOOLEAN类型解析异常',value);
            }
          }
        };
        try {
          if(change_list[type]){
            change_list[type](val);
          }else {
            _.log('试验数据类型解析失败',type,val);
          }
        } catch (error) {
          _.log(error,val,type);
        }
        return data;
      },
      getStorageKey:function(){
        return this.state.storage.name;
      },
      //更新本地数据（全量更新） 内存+storage
      updateLocalData:function(result){
        this.saveLocalData(result);
        if(this.state.storage.isSupport){
          var time = new Date().getTime();
          var data = {
            data: result,
            updateTime: time
          };
          window.localStorage.setItem(this.getStorageKey(),JSON.stringify(data));
        }
        _.log('更新试验数据成功'); 
      },
      //将试验结果保存在内存localdata中，将命中的所有experiment_id保存在localExpID，全量更新
      saveLocalData:function(results){
        this.localdata = results;
        SensorsABTest.localExpID = [];
        _.each(results,function(item){
          if(item.abtest_experiment_id){
            SensorsABTest.localExpID.push(item.abtest_experiment_id);
          }
        });
      },
      //初始化分流API
      initMethods: function(context){
        var methods = ['asyncFetchABTest','fastFetchABTest','fetchCacheABTest'];
        _.each(methods,function(key){
          SensorsABTest[key] = context.methods[key];
        });
      },
      //未初始化或者初始化失败分流API不支持调用
      asyncFetchABTest:function(){
        SensorsABTest.error('asyncFetchABTest调用失败,A/B Testing未初始化');
      },
      fastFetchABTest:function(){
        SensorsABTest.error('fastFetchABTest调用失败,A/B Testing未初始化');   
      },
      fetchCacheABTest:function(){
        SensorsABTest.error('fetchCacheABTest调用失败,A/B Testing未初始化');   
      },
      //获取本地试验ID
      getLocalList:function(){
        return this.localExpID;
      },

    
     /**
      * 初始化如果storage中有试验数据并且是10分钟内拉取的则不再拉取使用缓存。拉取如果失败重试3次。
      * @param {Function} fetchFunc 拉取方法（服务器/App）
      * @param {object} context 拉取方法执行的上下文对象
      */
      initFetch:function(fetchFunc,context){
        var data = null;
        if(SensorsABTest.state.storage.isSupport){
          data = window.localStorage.getItem(SensorsABTest.getStorageKey());
        }
        if(!data){
          _.log('初始化 SDK，尝试获取试验数据');
          fetchFunc.call(context);
        }else {
          data = JSON.parse(data);
          this.saveLocalData(data.data);

          var last_time = data.updateTime;
          var now_time = new Date().getTime();
          if((_.isNumber(last_time) && now_time - last_time >= this.para.update_interval) || now_time - last_time <= 0 || !_.isNumber(last_time)){
            _.log('初始化 SDK，尝试获取试验数据',last_time,now_time);
            fetchFunc.call(context);
          }
        }
        //每 10 分钟拉取一次
        setInterval(function(){
          fetchFunc.call(context);
        },SensorsABTest.para.update_interval);
      },

      
      /**
       * 查找localdata中是否有该试验ID的数据，没有返回null
       * @param {String} experimentID 试验ID
       * return 试验结果对象
       */
      searchLocalExp:function(experimentID){
        var exp = null;
        _.each(SensorsABTest.localdata,function(item){
          
          if(_.isObject(item) && item.abtest_experiment_id === experimentID){
            exp = item;
          }
        });
        return exp;
      },
      
      
      /**
       * 1、获取本地试验结果。localdata中有试验数据，数据类型与para.value_type一致才会返回试验值；否则返回默认值
       * 2、成功命中试验触发 $ABTestTrigger 事件
       * @param {Object} para 分流API参数 
       * @param {*} expObj 如果已获取试验对象传入
       * return 最终的试验value（试验值或默认值）
       */
      getExpResult:function(para,expObj){
        var result = para.default_value;
        var expObj = expObj ? expObj: SensorsABTest.searchLocalExp(para.experiment_id);
        
        if(_.isObject(expObj)){
          if(_.isObject(expObj.js_config)){
            if(expObj.js_config.type === para.value_type){
              result = expObj.js_config.value;
              //命中试验时如果不是调试用户则上报试验命中事件
              if(!expObj.is_white_list){
                SensorsABTest.trackTestTrigger(expObj,para.properties);
              }
            }else {
              this.log('试验结果类型与代码期望类型不一致，experiment_id：'+ para.experiment_id + '，当前返回类型为：'+ expObj.js_config.type + '，代码期望类型为：'+ para.value_type);
            }
          }
        }else {
          this.log('localdata未查询到试验数据，试验ID：' + para.experiment_id);
        }
        return result;
      },
      //sa必须初始化
      getSA:function(sd){
        if(_.isObject(sd) && _.isObject(sd.readyState) && sd.readyState.state>=3){
          this.sd = sd;
          return true;
        }else {
          return false;
        }
      },
      log:function(){
        if(typeof console === 'object' && console.log ){
            try{
                return console.log.apply(console,arguments);
            }catch(e){
                console.log(arguments[0]);
            }
        }
      },
      error:function(){
        if(typeof console === 'object' && console.error ){
          try{
              return console.error.apply(console,arguments);
          }catch(e){
              console.error(arguments[0]);
          }
        }
      },
      checkSADebug:function(){
        var abtest_url = _.getQueryParam(location.href,'sensors_abtest_url');
        var feature_code = _.getQueryParam(location.href,'feature_code');
        var account_id = +(_.getQueryParam(location.href,'account_id'));
        //从url上解析到这三个参数调试用户才能成功匹配
        if(abtest_url.length && feature_code.length && (_.isNumber(account_id) && account_id !== 0)){
          var data = {
            distinct_id : SensorsABTest.sd.store.getDistinctId(),
            //后端用来保存distinct_id的标识
            feature_code:feature_code,
            //number，登陆到SA的账号ID
            account_id:account_id
          };
          _.ajax({
            url: abtest_url,
            type: 'POST',
            data:JSON.stringify(data),
            credentials: false,
            contentType: 'application/json',
            timeout: SensorsABTest.para.timeout_milliseconds,
            cors: true,
            success:function(){},
            error: function(err) {
              _.log('distinct_id发送失败,err:',err);
            }
          });
        }
      },
      //入口函数
      init:function(sd,para){
          
          //避免重复初始化
          if(this.sd){
            this.log('A/B Testing SDK 重复初始化！只有第一次初始化有效！');
            return false;
          }

          _ = sd._;
          _.log = sd.log;

          //确保JS SDK初始化完成
          if(!this.getSA(sd)){
            this.log('A/B Testing 初始化失败,Web JS SDK 没有初始化完成');
            return false;
          }

          if(!_.isObject(para)){
            _.log('A/B Testing SDK 初始化失败，请传入正确的初始化参数!para:',para);
            return false;
          }
          
          //不支持storage无法使用缓存
          if(typeof localStorage !== 'object' || !this.sd._.localStorage.isSupport()){
            _.log('localstorage异常');
            this.state.storage.isSupport = false;
          }
    

          //根据打通状态选择不同的初始化模式
          if(sd.bridge.is_verify_success){
            console.log('打通逻辑');
            this.bridgeStore.init(para);
          }else {
            console.log('未打通逻辑');
            this.normalStore.init(para);
          }
      },
      //打通逻辑
      bridgeStore:{
        init:function(para){
          //参数校验
          if(!this.setPara(para)){
            return false;
          }
          _.log('A/B Testing SDK 初始化成功');

          //打通情况下的storage
          SensorsABTest.state.storage.name = 'sawebjssdkabtest_bridge';


          //创建abtest bridge实例
          SensorsABTest.abBridge = new SensorsABTest.sd.JSBridge({
            type:'abtest',
            app_call_js:function(data){
              data = JSON.parse(data);
              if(data.message_id){
                this.double(data);
              }
            }
          });

          //判断App abtest是否可用
          if(_.isObject(window.SensorsData_iOS_JS_Bridge) && window.SensorsData_iOS_JS_Bridge.sensorsdata_abtest_module && SensorsABTest.abBridge.hasAppBridge()){
            SensorsABTest.bridgeState = 'ab_bridge_ok';
          }else if(_.isObject(window.SensorsData_APP_New_H5_Bridge) && _.isFunction(window.SensorsData_APP_New_H5_Bridge.sensorsdata_abtest_module) && window.SensorsData_APP_New_H5_Bridge.sensorsdata_abtest_module() && SensorsABTest.abBridge.hasAppBridge()){
            SensorsABTest.bridgeState = 'ab_bridge_ok';
          }else {
            SensorsABTest.bridgeState = 'ab_no_abtest_bridge';
          }

          //初始化拉取试验数据
          SensorsABTest.initFetch(this.getResultFromApp,this);

          //初始化分流 API
          SensorsABTest.initMethods(this);

        },
        //设置全局参数，打通H5目前仅限制timeout
        setPara:function(para){
          var verObj = SensorsABTest.verifyStore.para('打通初始化',para,{
            timeout_milliseconds:'not_essential'
          });
          SensorsABTest.para = _.extend({},SensorsABTest.default_para,verObj.para);
          if(!_.isBoolean(SensorsABTest.para.collect_bridge_status)){
            SensorsABTest.para.collect_bridge_status = true;
          }
          if(!_.isNumber(SensorsABTest.para.update_interval)){
            SensorsABTest.para.update_interval = 600000;
          }
          return true;
        },
        getResultFromApp:function(obj){
          obj = _.isObject(obj) ? obj : {};
          var para = obj.para || {};
          var suc = obj.suc;
          var err = obj.err;
          var time_out = para.timeout_milliseconds || SensorsABTest.para.timeout_milliseconds;
          function sendRequest(){
            if(SensorsABTest.bridgeState === 'ab_bridge_ok'){
              SensorsABTest.abBridge.requestToApp({
                data:{
                  properties:para.properties,
                  timeout: time_out
                },
                callback:function(data){
                  if(_.isObject(data) && _.isObject(data.data)){
                    _.log('成功获取到 App 端返回的试验数据','data:',data);
                    SensorsABTest.dealResponseData(data.data);
                    if(suc){
                      suc(data);
                    }
                  }else {
                    //App端网络异常或者url错误等导致的失败
                    _.log('App 端请求失败');
                    if(err){
                      err();
                    } 
                  }
                },
                timeout:{
                  time:time_out,
                  callback:function(){
                    _.log('获取App端数据失败');
                    if(err){
                      err();
                    }                
                  }
                }
              });
            }else {
              if(err){
                _.log('A/B Testing 打通失败，',SensorsABTest.bridgeState);
                err();
              }
            }
          }
          sendRequest();

        },
        asyncFetch:function(para){
          SensorsABTest.bridgeStore.getResultFromApp({
            para:para,
            suc:function(data){
              if(_.isObject(data.properties)){
                para.properties = _.extend(data.properties,para.properties);
              }            if(data.data.status === 'SUCCESS'){
                var result = SensorsABTest.getExpResult(para);
                para.callback(result);
              }else {
                para.callback(para.default_value); 
              }
            },
            err:function(){
              para.callback(para.default_value);
            }
          });
        },
        methods:{
          asyncFetchABTest:function(para){
            if(!_.isObject(para)){
              SensorsABTest.error('asyncFetchABTest调用失败，参数未正确配置');
              return false;
            }                   
            var verifyobj = SensorsABTest.verifyStore.para('asyncFetchABTest',para,{
              experiment_id:'essential',
              value_type:'essential',
              default_value:'essential',
              callback:'not_essential',
              timeout_milliseconds:'not_essential',
              properties:'not_essential'
            });
     
            //参数校验没通过，方法不能调用
            if(!verifyobj.verify_success){
              return ;
            }
            para = verifyobj.para;

            if(SensorsABTest.bridgeState === 'ab_bridge_ok'){
              SensorsABTest.bridgeStore.asyncFetch(para);
            }else {
              para.callback(para.default_value);
            }


          },
          fastFetchABTest:function(para){
            if(!_.isObject(para)){
              SensorsABTest.error('fastFetchABTest调用失败，参数未正确配置');
              return false;
            }          var verifyobj = SensorsABTest.verifyStore.para('fastFetchABTest',para,{
              experiment_id:'essential',
              value_type:'essential',
              default_value:'essential',
              callback:'not_essential',
              timeout_milliseconds:'not_essential',
              properties:'not_essential'
            });
     
            //参数校验没通过，方法不能调用
            if(!verifyobj.verify_success){
              return ;
            }
       
            para = verifyobj.para;

            //先从本地获取，本地取不到再发起请求
            var expObj = SensorsABTest.searchLocalExp(para.experiment_id);
            if(_.isObject(expObj)){
              var result = SensorsABTest.getExpResult(para,expObj);
              para.callback(result);
            }else {
              _.log('fastFetchABTest缓存中未读取到数据，发起请求');
              if(SensorsABTest.bridgeState === 'ab_bridge_ok'){
                SensorsABTest.bridgeStore.asyncFetch(para);
              }else {
                para.callback(para.default_value);
              }

            }

          },
          //从缓存获取试验数据
          fetchCacheABTest:function(para){
            if(!_.isObject(para)){
              SensorsABTest.error('fetchCacheABTest调用失败，参数未正确配置');
              return ;
            }          var verifyobj = SensorsABTest.verifyStore.para('fetchCacheABTest',para,{
              experiment_id:'essential',
              value_type:'essential',
              default_value:'essential'
            });
     
            //参数校验没通过，方法不能调用
            if(!verifyobj.verify_success){
              return;
            }
            return SensorsABTest.getExpResult(verifyobj.para); 
          }
        }
      },
      //非打通逻辑
      normalStore:{
        init: function(para){
          //参数校验
          if(!this.setPara(para)){
              return false;
          }

          SensorsABTest.bridgeState = 'ab_no_host_bridge';

          //初始化platform，移动端为 H5
          if(/Android|webOS|iPhone|iPod|BlackBerry/i.test(navigator.userAgent)) {
            SensorsABTest.state.platform = 'H5';
          } else {
            SensorsABTest.state.platform = 'Web';
          }
          

          _.log('A/B Testing SDK 初始化成功，试验 URL：',para.url);
          SensorsABTest.checkSADebug();


          //初始化拉取试验数据
          SensorsABTest.initFetch(this.getResultFromServer,this);

          //初始化分流 API
          SensorsABTest.initMethods(this);
        
        },
        
        //设置全局参数
        setPara: function(para){   

          /* 检查url参数配置
              1、协议
              2、必须有project_key
          */

          if(!_.isString(para.url) || para.url.slice(0,4) !== 'http'){
              _.log('A/B Testing SDK 初始化失败，请使用正确的 URL！');
              return false;
          }else {
              if(location.protocol.slice(0,4) === 'https' && para.url.slice(0,4) === 'http'){
                  _.log('A/B Testing SDK 初始化失败，https 页面必须使用 https 的 URL');
                  return false;
              }
          }
          var project_key = _.getQueryParam(para.url,'project-key');
          if(!project_key){
            _.log('A/B Testing SDK 初始化失败，请使用正确的 URL（必须包含 project-key）！');
            return false;
          }else {
            para.project_key = project_key;
            var index = para.url.indexOf('?');
            para.path = para.url.slice(0,index);
          }
          var verObj = SensorsABTest.verifyStore.para('A/B Testing SDK 初始化',para,{
            timeout_milliseconds:'not_essential'
          });
          
          //合并参数
          SensorsABTest.para = _.extend({},SensorsABTest.default_para,verObj.para);
          
          if(!_.isBoolean(SensorsABTest.para.collect_bridge_status)){
            SensorsABTest.para.collect_bridge_status = true;
          }
          if(!_.isNumber(SensorsABTest.para.update_interval)){
            SensorsABTest.para.update_interval = 600000;
          }
          return true;

        },
        asyncFetch:function(para){
          SensorsABTest.normalStore.getResultFromServer({
            para:para,
            suc:function(data){
              if(_.isObject(data) && data.status === 'SUCCESS'){
                var result = SensorsABTest.getExpResult(para);
                para.callback(result);
              }else {
                para.callback(para.default_value); 
              }
            },
            err:function(){
              para.callback(para.default_value);
            }
          });
        },
        methods:{
          asyncFetchABTest:function(para){
            if(!_.isObject(para)){
              SensorsABTest.error('asyncFetchABTest调用失败，参数未正确配置');
              return false;
            }                   
            var verifyobj = SensorsABTest.verifyStore.para('asyncFetchABTest',para,{
              experiment_id:'essential',
              value_type:'essential',
              default_value:'essential',
              callback:'not_essential',
              timeout_milliseconds:'not_essential',
              properties:'not_essential'
            });
     
            //参数校验没通过，方法不能调用
            if(!verifyobj.verify_success){
              return ;
            }
            para = verifyobj.para;
            
            //请求获取试验结果
            SensorsABTest.normalStore.asyncFetch(para);
           
          },
          fastFetchABTest:function(para){
            if(!_.isObject(para)){
              SensorsABTest.error('fastFetchABTest调用失败，参数未正确配置');
              return false;
            }          var verifyobj = SensorsABTest.verifyStore.para('fastFetchABTest',para,{
              experiment_id:'essential',
              value_type:'essential',
              default_value:'essential',
              callback:'not_essential',
              timeout_milliseconds:'not_essential',
              properties:'not_essential'
            });
     
            //参数校验没通过，方法不能调用
            if(!verifyobj.verify_success){
              return ;
            }
            para = verifyobj.para;

            //先从本地获取，本地取不到再发起请求
            var expObj = SensorsABTest.searchLocalExp(para.experiment_id);
            if(_.isObject(expObj)){
              var result = SensorsABTest.getExpResult(para,expObj);
              para.callback(result);
            }else {
              _.log('fastFetchABTest缓存中未读取到数据，发起请求');
              SensorsABTest.normalStore.asyncFetch(para);
            }

          },
          //从缓存获取试验数据
          fetchCacheABTest:function(para){
            if(!_.isObject(para)){
              SensorsABTest.error('fetchCacheABTest调用失败，参数未正确配置');
              return ;
            }          var verifyobj = SensorsABTest.verifyStore.para('fetchCacheABTest',para,{
              experiment_id:'essential',
              value_type:'essential',
              default_value:'essential'
            });
     
            //参数校验没通过，方法不能调用
            if(!verifyobj.verify_success){
              return;
            }
            return SensorsABTest.getExpResult(verifyobj.para); 
          }
        },
        //生成 requestData
        creatRequestData: function(para){
          var anonymous_id = '';
          if(!_.isEmptyObject(SensorsABTest.sd.store._state)){
              anonymous_id = SensorsABTest.sd.store._state._first_id || SensorsABTest.sd.store._state.first_id || SensorsABTest.sd.store._state._distinct_id || SensorsABTest.sd.store._state.distinct_id;
          }
          var data = {
            anonymous_id:anonymous_id,
            platform: SensorsABTest.state.platform,
            properties: _.isObject(para.properties) ? para.properties : {}
          };
          if(SensorsABTest.sd.store._state.first_id){
            data.login_id = SensorsABTest.sd.store.getDistinctId();
          }
          return data;
        },
        //发送分流请求获取试验数据
        getResultFromServer:function(obj){
          obj = _.isObject(obj) ? obj : {};
          var para = obj.para || {};
          var suc = obj.suc;
          var err = obj.err;
          var data = this.creatRequestData(para);
          function sendRequest(){
            _.ajax({
              url: SensorsABTest.para.path,
              type: 'POST',
              data:JSON.stringify(data),
              credentials: false,
              contentType: 'application/json',
              timeout: para.timeout_milliseconds || SensorsABTest.para.timeout_milliseconds,
              cors: true,
              header:{
                'project-key':SensorsABTest.para.project_key
              },
              success:function(data){
                _.log('获取到服务端数据',data);
                SensorsABTest.dealResponseData(data);
                if(suc){
                  suc(data);
                }
              },
              error: function(error) {
                _.log('服务端请求发送失败',error);
                if(err){
                  err();
                }
              }
            });
          }
          sendRequest();

        }
      }
  };



  if(window.SensorsDataWebJSSDKPlugin && Object.prototype.toString.call(window.SensorsDataWebJSSDKPlugin) == '[object Object]'){
      window.SensorsDataWebJSSDKPlugin.SensorsABTest = window.SensorsDataWebJSSDKPlugin.SensorsABTest || SensorsABTest;
  }else {
      window.SensorsDataWebJSSDKPlugin = {
        SensorsABTest : SensorsABTest
      };
  }

  return SensorsABTest;

})));
