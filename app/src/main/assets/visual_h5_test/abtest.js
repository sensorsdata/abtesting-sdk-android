(function (global, factory) {
  typeof exports === 'object' && typeof module !== 'undefined' ? module.exports = factory() : factory();
}(this, (function () { 'use strict';

  /*
  一系列的工具方法
  */
  var _ = {};
  var ArrayProto = Array.prototype;
  var ObjProto = Object.prototype;
  var slice = ArrayProto.slice;
  var toString = ObjProto.toString;
  var hasOwnProperty = ObjProto.hasOwnProperty;
  var nativeForEach = ArrayProto.forEach;
  var nativeIsArray = Array.isArray;
  var breaker = {};

  _.each = function (obj, iterator, context) {
    if (obj == null) {
      return false;
    }
    if (nativeForEach && obj.forEach === nativeForEach) {
      obj.forEach(iterator, context);
    } else if (_.isArray(obj) && obj.length === +obj.length) {
      for (var i = 0, l = obj.length; i < l; i++) {
        if (i in obj && iterator.call(context, obj[i], i, obj) === breaker) {
          return false;
        }
      }
    } else {
      for (var key in obj) {
        if (hasOwnProperty.call(obj, key)) {
          if (iterator.call(context, obj[key], key, obj) === breaker) {
            return false;
          }
        }
      }
    }
  };

  // 普通的extend，不能到二级
  _.extend = function (obj) {
    _.each(slice.call(arguments, 1), function (source) {
      for (var prop in source) {
        if (hasOwnProperty.call(source, prop) && source[prop] !== void 0) {
          obj[prop] = source[prop];
        }
      }
    });
    return obj;
  };
  _.isArray =
    nativeIsArray ||
    function (obj) {
      return toString.call(obj) === '[object Array]';
    };

  _.isObject = function (obj) {
    if (obj == null) {
      return false;
    } else {
      return toString.call(obj) == '[object Object]';
    }
  };
  _.isElement = function (obj) {
    return !!(obj && obj.nodeType === 1);
  };
  _.isString = function (obj) {
    return toString.call(obj) == '[object String]';
  };
  _.isBoolean = function (obj) {
    return toString.call(obj) == '[object Boolean]';
  };

  _.isNumber = function (obj) {
    return toString.call(obj) == '[object Number]' && /[\d\.]+/.test(String(obj));
  };

  // 数组去重复
  _.unique = function (ar) {
    var temp,
      n = [],
      o = {};
    for (var i = 0; i < ar.length; i++) {
      temp = ar[i];
      if (!(temp in o)) {
        o[temp] = true;
        n.push(temp);
      }
    }
    return n;
  };
  // gbk等编码decode会异常
  _.decodeURIComponent = function (val) {
    var result = val;
    try {
      result = decodeURIComponent(val);
    } catch (e) {
      result = val;
    }
    return result;
  };

  _.getQueryParam = function (url, param) {
    param = param.replace(/[\[]/, '\\[').replace(/[\]]/, '\\]');
    url = _.decodeURIComponent(url);
    var regexS = '[\\?&]' + param + '=([^&#]*)',
      regex = new RegExp(regexS),
      results = regex.exec(url);
    if (results === null || (results && typeof results[1] !== 'string' && results[1].length)) {
      return '';
    } else {
      return _.decodeURIComponent(results[1]);
    }
  };

  _.indexOf = function (arr, target) {
    var indexof = arr.indexOf;
    if (indexof) {
      return indexof.call(arr, target);
    } else {
      for (var i = 0; i < arr.length; i++) {
        if (target === arr[i]) {
          return i;
        }
      }
      return -1;
    }
  };

  _.isFunction = function (f) {
    if (!f) {
      return false;
    }
    var type = Object.prototype.toString.call(f);
    return type == '[object Function]' || type == '[object AsyncFunction]';
  };

  _.isEmptyObject = function (obj) {
    if (_.isObject(obj)) {
      for (var key in obj) {
        if (hasOwnProperty.call(obj, key)) {
          return false;
        }
      }
      return true;
    }
    return false;
  };

  _.map = function (obj, iterator) {
    var results = [];
    // Not using strict equality so that this acts as a
    // shortcut to checking for `null` and `undefined`.
    if (obj == null) {
      return results;
    }
    if (Array.prototype.map && obj.map === Array.prototype.map) {
      return obj.map(iterator);
    }
    _.each(obj, function (value, index, list) {
      results.push(iterator(value, index, list));
    });
    return results;
  };

  _.base64Decode = function (data) {
    var arr = _.map(atob(data).split(''), function (c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    });
    return decodeURIComponent(arr.join(''));
  };

  /**
   * 查询得到URL参数
   * @param {string} queryString - 以问号开头的 query string
   * @return {Object} 一个含有参数列表的key/value对象
   *
   * @example
   * var url = _.getURLSearchParams('?project=testproject&query1=test&silly=willy&field[0]=zero&field[2]=two#test=hash&chucky=cheese');
   *
   * url.project; // => testproject
   */
  _.getURLSearchParams = function (queryString) {
    queryString = queryString || '';
    var decodeParam = function (str) {
      return decodeURIComponent(str);
    };
    var args = {}; // Start with an empty object
    var query = queryString.substring(1); // Get query string, minus '?'
    var pairs = query.split('&'); // Split at ampersands
    for (var i = 0; i < pairs.length; i++) {
      // For each fragment
      var pos = pairs[i].indexOf('='); // Look for "name=value"
      if (pos === -1) continue; // If not found, skip it
      var name = pairs[i].substring(0, pos); // Extract the name
      var value = pairs[i].substring(pos + 1); // Extract the value
      name = decodeParam(name); // Decode the name
      value = decodeParam(value); // Decode the value
      args[name] = value; // Store as a property
    }
    return args; // Return the parsed arguments
  };

  _.urlParse = function (para) {
    var URLParser = function (a) {
      this._fields = {
        Username: 4,
        Password: 5,
        Port: 7,
        Protocol: 2,
        Host: 6,
        Path: 8,
        URL: 0,
        QueryString: 9,
        Fragment: 10
      };
      this._values = {};
      this._regex = null;
      this._regex = /^((\w+):\/\/)?((\w+):?(\w+)?@)?([^\/\?:]+):?(\d+)?(\/?[^\?#]+)?\??([^#]+)?#?(\w*)/;

      if (typeof a != 'undefined') {
        this._parse(a);
      }
    };
    URLParser.prototype.setUrl = function (a) {
      this._parse(a);
    };
    URLParser.prototype._initValues = function () {
      for (var a in this._fields) {
        this._values[a] = '';
      }
    };
    URLParser.prototype.addQueryString = function (queryObj) {
      if (typeof queryObj !== 'object') {
        return false;
      }
      var query = this._values.QueryString || '';
      for (var i in queryObj) {
        if (new RegExp(i + '[^&]+').test(query)) {
          query = query.replace(new RegExp(i + '[^&]+'), i + '=' + queryObj[i]);
        } else {
          if (query.slice(-1) === '&') {
            query = query + i + '=' + queryObj[i];
          } else {
            if (query === '') {
              query = i + '=' + queryObj[i];
            } else {
              query = query + '&' + i + '=' + queryObj[i];
            }
          }
        }
      }
      this._values.QueryString = query;
    };
    URLParser.prototype.getUrl = function () {
      var url = '';
      url += this._values.Origin;
      url += this._values.Port ? ':' + this._values.Port : '';
      url += this._values.Path;
      url += this._values.QueryString ? '?' + this._values.QueryString : '';
      url += this._values.Fragment ? '#' + this._values.Fragment : '';
      return url;
    };

    URLParser.prototype.getUrl = function () {
      var url = '';
      url += this._values.Origin;
      url += this._values.Port ? ':' + this._values.Port : '';
      url += this._values.Path;
      url += this._values.QueryString ? '?' + this._values.QueryString : '';
      return url;
    };
    URLParser.prototype._parse = function (a) {
      this._initValues();
      var b = this._regex.exec(a);
      if (!b) {
        _.log('DPURLParser::_parse -> Invalid URL');
      }
      for (var c in this._fields) {
        if (typeof b[this._fields[c]] != 'undefined') {
          this._values[c] = b[this._fields[c]];
        }
      }
      this._values['Hostname'] = this._values['Host'].replace(/:\d+$/, '');
      this._values['Origin'] = this._values['Protocol'] + '://' + this._values['Hostname'];
    };
    return new URLParser(para);
  };

  /**
   * 解析URL
   * @param {string} url
   * @return {Object} 一个 URL 对象或者普通JS对象
   *
   * @example
   * var url = _.URL('http://www.domain.com:8080/path/index.html?project=testproject&query1=test&silly=willy&field[0]=zero&field[2]=two#test=hash&chucky=cheese');
   *
   * url.hostname; // => www.domain.com
   * url.searchParams.get('project'); // => testproject
   */
  _.URL = function (url) {
    var result = {};
    // Some browsers allow objects to be created via URL constructor, but instances do not have the expected url properties.
    // See https://www.caniuse.com/#feat=url
    var isURLAPIWorking = function () {
      var url;
      try {
        url = new URL('http://modernizr.com/');
        return url.href === 'http://modernizr.com/';
      } catch (e) {
        return false;
      }
    };
    if (typeof window.URL === 'function' && isURLAPIWorking()) {
      result = new URL(url);
      if (!result.searchParams) {
        result.searchParams = (function () {
          var params = _.getURLSearchParams(result.search);
          return {
            get: function (searchParam) {
              return params[searchParam];
            }
          };
        })();
      }
    } else {
      var _regex = /^https?:\/\/.+/;
      if (_regex.test(url) === false) {
        _.log('Invalid URL');
      }
      var instance = _.urlParse(url);
      result.hash = '';
      result.host = instance._values.Host ? instance._values.Host + (instance._values.Port ? ':' + instance._values.Port : '') : '';
      result.href = instance._values.URL;
      result.password = instance._values.Password;
      result.pathname = instance._values.Path;
      result.port = instance._values.Port;
      result.search = instance._values.QueryString ? '?' + instance._values.QueryString : '';
      result.username = instance._values.Username;
      result.hostname = instance._values.Hostname;
      result.protocol = instance._values.Protocol ? instance._values.Protocol + ':' : '';
      result.origin = instance._values.Origin ? instance._values.Origin + (instance._values.Port ? ':' + instance._values.Port : '') : '';
      result.searchParams = (function () {
        var params = _.getURLSearchParams('?' + instance._values.QueryString);
        return {
          get: function (searchParam) {
            return params[searchParam];
          }
        };
      })();
    }
    return result;
  };

  _.getQueryParam = function (url, param) {
    param = param.replace(/[\[]/, '\\[').replace(/[\]]/, '\\]');
    url = _.decodeURIComponent(url);
    var regexS = '[\\?&]' + param + '=([^&#]*)',
      regex = new RegExp(regexS),
      results = regex.exec(url);
    if (results === null || (results && typeof results[1] !== 'string' && results[1].length)) {
      return '';
    } else {
      return _.decodeURIComponent(results[1]);
    }
  };

  _.isEmptyObject = function (obj) {
    if (_.isObject(obj)) {
      for (var key in obj) {
        if (hasOwnProperty.call(obj, key)) {
          return false;
        }
      }
      return true;
    }
    return false;
  };

  _.contentLoaded = function (win, fn) {
    var done = false,
      top = true,
      doc = win.document,
      root = doc.documentElement,
      modern = doc.addEventListener,
      add = modern ? 'addEventListener' : 'attachEvent',
      rem = modern ? 'removeEventListener' : 'detachEvent',
      pre = modern ? '' : 'on',
      init = function (e) {
        if (e.type == 'readystatechange' && doc.readyState != 'complete') return;
        (e.type == 'load' ? win : doc)[rem](pre + e.type, init, false);
        if (!done && (done = true)) fn.call(win, e.type || e);
      },
      poll = function () {
        try {
          root.doScroll('left');
        } catch (e) {
          setTimeout(poll, 50);
          return;
        }
        init('poll');
      };

    if (doc.readyState == 'complete') fn.call(win, 'lazy');
    else {
      if (!modern && root.doScroll) {
        try {
          top = !win.frameElement;
        } catch (e) {}
        if (top) poll();
      }
      doc[add](pre + 'DOMContentLoaded', init, false);
      doc[add](pre + 'readystatechange', init, false);
      win[add](pre + 'load', init, false);
    }
  };
  _.secCheck = {
    isHttpUrl: function (str) {
      if (typeof str !== 'string') return false;
      var _regex = /^https?:\/\/.+/;
      if (_regex.test(str) === false) {
        _.log('Invalid URL');
        return false;
      }
      return true;
    },
    removeScriptProtocol: function (str) {
      if (typeof str !== 'string') return '';
      var _regex = /^\s*javascript/i;
      while (_regex.test(str)) {
        str = str.replace(_regex, '');
      }
      return str;
    }
  };
  _.addEvent = function () {
    function fixEvent(event) {
      if (event) {
        event.preventDefault = fixEvent.preventDefault;
        event.stopPropagation = fixEvent.stopPropagation;
        event._getPath = fixEvent._getPath;
      }
      return event;
    }
    fixEvent._getPath = function () {
      var ev = this;
      var polyfill = function () {
        try {
          var element = ev.target;
          var pathArr = [element];
          if (element === null || element.parentElement === null) {
            return [];
          }
          while (element.parentElement !== null) {
            element = element.parentElement;
            pathArr.unshift(element);
          }
          return pathArr;
        } catch (err) {
          return [];
        }
      };
      return this.path || (this.composedPath && this.composedPath()) || polyfill();
    };
    fixEvent.preventDefault = function () {
      this.returnValue = false;
    };
    fixEvent.stopPropagation = function () {
      this.cancelBubble = true;
    };

    var register_event = function (element, type, handler) {
      var useCapture = true;
      if (element && element.addEventListener) {
        element.addEventListener(
          type,
          function (e) {
            e._getPath = fixEvent._getPath;
            handler.call(this, e);
          },
          useCapture
        );
      } else {
        var ontype = 'on' + type;
        var old_handler = element[ontype];
        element[ontype] = makeHandler(element, handler, old_handler);
      }
    };
    function makeHandler(element, new_handler, old_handlers) {
      var handler = function (event) {
        event = event || fixEvent(window.event);
        if (!event) {
          return undefined;
        }
        event.target = event.srcElement;

        var ret = true;
        var old_result, new_result;
        if (typeof old_handlers === 'function') {
          old_result = old_handlers(event);
        }
        new_result = new_handler.call(element, event);
        if (false === old_result || false === new_result) {
          ret = false;
        }
        return ret;
      };
      return handler;
    }

    register_event.apply(null, arguments);
  };
  _.addSinglePageEvent = function (callback) {
    var current_url = location.href;
    var historyPushState = window.history.pushState;
    var historyReplaceState = window.history.replaceState;

    //调用方法导致的url切换
    window.history.pushState = function () {
      historyPushState.apply(window.history, arguments);
      callback(current_url);
      current_url = location.href;
    };
    window.history.replaceState = function () {
      historyReplaceState.apply(window.history, arguments);
      callback(current_url);
      current_url = location.href;
    };

    // 前进后退导致的url切换
    var singlePageEvent = historyPushState ? 'popstate' : 'hashchange';
    _.addEvent(window, singlePageEvent, function () {
      callback(current_url);
      current_url = location.href;
    });
  };

  _.trim = function (str) {
    return str.replace(/^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g, '');
  };

  /**
   * 检查当前页面是否与多链接试验匹配
   * @param {URL} link 多链接试验分流URL
   * @param {string} type 匹配类型 STRICT：精确匹配，FUZZY:模糊匹配
   */
  _.checkUrlIsMatch = function (link, type) {
    var urlParse;
    var linkParse;
    var linkUrl = _.URL(link);
    if (type === 'STRICT') {
      if (location.href === linkUrl.href) {
        return true;
      } else {
        return false;
      }
    } else if (type === 'FUZZY') {
      try {
        urlParse = _.URL(location.href);
      } catch (error) {
        _.log('url 解析失败', error);
        return false;
      }
      try {
        linkParse = _.URL(link);
      } catch (error) {
        _.log('control_url 解析失败', error);
        return false;
      }
      if (urlParse.host === linkParse.host && urlParse.pathname === linkParse.pathname) {
        return true;
      } else {
        return false;
      }
    } else {
      _.log('link_match_type字段异常', type);
      return false;
    }
  };

  _.checkUrlIsRegexp = function (link, flags) {
    var urlParse;
    // 检测当前页面地址是否正常
    try {
      urlParse = _.URL(location.href);
    } catch (error) {
      _.log('url 解析失败', error);
      return false;
    }
    try {
      var link_regexp = flags ? new RegExp(link, flags) : new RegExp(link);
      var execArray = link_regexp.exec(urlParse.href);
      var is_regexp_success = true;
      if (!execArray) {
        return false;
      }
      // 每项都匹配则认为匹配成功
      _.each(execArray, function (val) {
        if (!val) {
          is_regexp_success = false;
        }
      });
      return is_regexp_success;
    } catch (error) {
      _.log('control_link字段异常', error);
      return false;
    }
  };

  _.log = function () {
    if (typeof console === 'object' && console.log) {
      if (_.isString(arguments[0])) {
        arguments[0] = 'sensorsabtest————' + arguments[0];
      }
      try {
        return console.log.apply(console, arguments);
      } catch (e) {
        console.log(arguments[0]);
      }
    }
  };
  _.error = function () {
    if (typeof console === 'object' && console.error) {
      try {
        return console.error.apply(console, arguments);
      } catch (e) {
        console.error(arguments[0]);
      }
    }
  };
  _.storage = {
    isSupport: function () {
      var supported = true;
      try {
        var supportName = '__sensorsdatasupport__';
        var val = 'testIsSupportStorage';
        window.localStorage.setItem(supportName, val);
        if (window.localStorage.getItem(supportName) !== val) {
          supported = false;
        }
        window.localStorage.removeItem(supportName);
      } catch (err) {
        supported = false;
      }
      return supported;
    },
    set: function (data, name) {
      if (this.isSupport()) {
        window.localStorage.setItem(name, data);
      }
    },
    get: function (name) {
      var data = null;
      if (this.isSupport()) {
        data = window.localStorage.getItem(name);
      }
      return data;
    }
  };

  _.formatDate = function (date) {
    function pad(n) {
      return n < 10 ? '0' + n : n;
    }
    return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate()) + ' ' + pad(date.getHours()) + ':' + pad(date.getMinutes()) + ':' + pad(date.getSeconds()) + '.' + pad(date.getMilliseconds());
  };
  _.isDate = function (arg) {
    return Object.prototype.toString.call(arg) == '[object Date]';
  };

  /**
   * 代码实验 API 逻辑
   */

  function NormalStore(SensorsABTest) {
    this.SensorsABTest = SensorsABTest;
    this.para = SensorsABTest.para;
  }
  //非打通逻辑
  NormalStore.prototype = {
    init: function (para) {
      //参数校验

      if (!this.setPara(para)) {
        return false;
      }
      this.bridgeState = 'ab_no_host_bridge';

      //初始化platform，移动端为 H5
      if (/Android|webOS|iPhone|iPod|BlackBerry/i.test(navigator.userAgent)) {
        this.SensorsABTest.state.platform = 'H5';
      } else {
        this.SensorsABTest.state.platform = 'Web';
      }

      this.SensorsABTest.log('A/B Testing SDK 初始化成功，试验 URL：', para.url);
      this.SensorsABTest.checkSADebug();

      this.SensorsABTest.store.init(this.getResultFromServer, this);
    },

    //设置全局参数
    setPara: function (para) {
      /* 检查url参数配置
             1、协议
             2、必须有project_key
         */

      if (!_.isString(para.url) || para.url.slice(0, 4) !== 'http') {
        this.SensorsABTest.log('A/B Testing SDK 初始化失败，请使用正确的 URL！');
        return false;
      } else {
        if (location.protocol.slice(0, 5) === 'https' && para.url.slice(0, 5) === 'http:') {
          this.SensorsABTest.log('A/B Testing SDK 初始化失败，https 页面必须使用 https 的 URL');
          return false;
        }
      }
      var project_key = _.getQueryParam(para.url, 'project-key');
      if (!project_key) {
        this.SensorsABTest.log('A/B Testing SDK 初始化失败，请使用正确的 URL（必须包含 project-key）！');
        return false;
      } else {
        para.project_key = project_key;
      }
      var verObj = this.SensorsABTest.verifyStore.para('A/B Testing SDK 初始化', para, {
        timeout_milliseconds: 'not_essential'
      });

      //合并参数
      this.SensorsABTest.para = _.extend({}, this.SensorsABTest.default_para, verObj.para);

      if (!_.isBoolean(this.SensorsABTest.para.collect_bridge_status)) {
        this.SensorsABTest.para.collect_bridge_status = true;
      }
      if (!_.isNumber(this.SensorsABTest.para.update_interval)) {
        this.SensorsABTest.para.update_interval = 600000;
      }
      if (this.SensorsABTest.sd.para.encrypt_cookie === true) {
        this.SensorsABTest.para.encrypt_cookie = true;
      }
      if (this.SensorsABTest.sd.para.sdk_id) {
        this.SensorsABTest.state.storage.name += '_' + this.SensorsABTest.sd.para.sdk_id;
      }
      return true;
    },
    asyncFetch: function (para) {
      var _this = this;
      this.SensorsABTest.normalStore.getResultFromServer({
        para: para,
        suc: function (data) {
          if (_.isObject(data) && data.status === 'SUCCESS') {
            var result = _this.SensorsABTest.getExpResult(para);
            para.callback(result);
          } else {
            para.callback(para.default_value);
          }
        },
        err: function () {
          para.callback(para.default_value);
        }
      });
    },
    //生成 requestData
    creatRequestData: function (para) {
      var anonymous_id = '';
      if (!_.isEmptyObject(this.SensorsABTest.sd.store._state)) {
        anonymous_id = this.SensorsABTest.sd.store._state._first_id || this.SensorsABTest.sd.store._state.first_id || this.SensorsABTest.sd.store._state._distinct_id || this.SensorsABTest.sd.store._state.distinct_id;
      }
      var data = {
        anonymous_id: anonymous_id,
        platform: this.SensorsABTest.state.platform,
        abtest_lib_version: this.SensorsABTest.lib_version,
        properties: {
          $is_first_day: this.SensorsABTest.sd._.cookie.getNewUser()
        }
      };
      if (_.isObject(para.properties)) {
        data.properties = _.extend({}, data.properties, para.properties);
      }
      if (_.isObject(para.custom_properties)) {
        data.custom_properties = _.extend({}, para.custom_properties);
        data.param_name = para.param_name;
      }
      if (this.SensorsABTest.sd.store._state.first_id) {
        data.login_id = this.SensorsABTest.sd.store.getDistinctId();
      }
      return data;
    },
    //发送分流请求获取试验数据
    getResultFromServer: function (obj) {
      var _this = this;
      obj = _.isObject(obj) ? obj : {};
      var para = obj.para || {};
      var suc = obj.suc;
      var err = obj.err;
      var data = this.creatRequestData(para);
      function sendRequest() {
        var request_id = _this.SensorsABTest.sd.store.getDistinctId();
        _this.SensorsABTest.sd._.ajax({
          url: _this.SensorsABTest.para.url,
          type: 'POST',
          data: JSON.stringify(data),
          credentials: false,
          contentType: 'application/json',
          timeout: para.timeout_milliseconds || _this.SensorsABTest.para.timeout_milliseconds,
          cors: true,
          success: function (data) {
            _this.SensorsABTest.dealResponseData(data, request_id);
            if (suc) {
              suc(data);
            }
            _this.SensorsABTest.fetchData.setNextFetch();
          },
          error: function (error) {
            _this.SensorsABTest.log('服务端请求发送失败', error);
            if (err) {
              err();
            }
            _this.SensorsABTest.fetchData.setNextFetch();
          }
        });
      }
      this.SensorsABTest.log('向服务端发起试验请求');
      sendRequest();
    }
  };
  NormalStore.prototype.methods = {
    asyncFetchABTest: function (para) {
      if (!_.isObject(para)) {
        this.SensorsABTest.log('asyncFetchABTest调用失败，参数未正确配置');
        return false;
      }
      var verifyobj = this.SensorsABTest.verifyStore.para('asyncFetchABTest', para, {
        param_name: 'essential',
        value_type: 'essential',
        default_value: 'essential',
        callback: 'essential',
        timeout_milliseconds: 'not_essential',
        properties: 'not_essential'
      });

      //参数校验没通过，方法不能调用
      if (!verifyobj.verify_success) {
        return;
      }
      para = verifyobj.para;

      var verifyCustom = this.SensorsABTest.verifyStore.resolveCustomProperties(para);

      //参数校验没通过，返回默认值
      if (!verifyCustom.verify_success) {
        para.callback(para.default_value);
        return;
      }

      para = verifyCustom.para;

      //请求获取试验结果
      this.SensorsABTest.normalStore.asyncFetch(para);
    },
    fastFetchABTest: function (para) {
      if (!_.isObject(para)) {
        this.SensorsABTest.log('fastFetchABTest调用失败，参数未正确配置');
        return false;
      }

      var verifyobj = this.SensorsABTest.verifyStore.para('fastFetchABTest', para, {
        param_name: 'essential',
        value_type: 'essential',
        default_value: 'essential',
        callback: 'essential',
        timeout_milliseconds: 'not_essential',
        properties: 'not_essential'
      });

      //参数校验没通过，方法不能调用
      if (!verifyobj.verify_success) {
        return;
      }
      para = verifyobj.para;

      //先从本地获取，本地取不到再发起请求
      var expObj = this.SensorsABTest.searchLocalExp(para.param_name);
      if (_.isObject(expObj)) {
        var result = this.SensorsABTest.getExpResult(para, expObj);
        para.callback(result);
        return;
      }

      var verifyCustom = this.SensorsABTest.verifyStore.resolveCustomProperties(para);

      //参数校验没通过，返回默认值
      if (!verifyCustom.verify_success) {
        para.callback(para.default_value);
        return;
      }

      para = verifyCustom.para;

      this.SensorsABTest.log('fastFetchABTest缓存中未读取到数据，发起请求');
      this.SensorsABTest.normalStore.asyncFetch(para);
    },
    //从缓存获取试验数据
    fetchCacheABTest: function (para) {
      if (!_.isObject(para)) {
        this.SensorsABTest.log('fetchCacheABTest调用失败，参数未正确配置');
        return;
      }
      var verifyobj = this.SensorsABTest.verifyStore.para('fetchCacheABTest', para, {
        param_name: 'essential',
        value_type: 'essential',
        default_value: 'essential'
      });

      //参数校验没通过，方法不能调用
      if (!verifyobj.verify_success) {
        return;
      }
      return this.SensorsABTest.getExpResult(verifyobj.para);
    }
  };

  /**
   * 代码实验 API 逻辑
   */
  function BridgeStore(SensorsABTest) {
    this.SensorsABTest = SensorsABTest;
    this.para = SensorsABTest.para;
  }
  //打通逻辑
  BridgeStore.prototype = {
    init: function (para) {
      var _this = this;
      this.SensorsABTest.state.storage.name = 'sawebjssdkabtest_bridge';

      //参数校验
      if (!this.setPara(para)) {
        return false;
      }
      this.SensorsABTest.log('A/B Testing SDK 初始化成功');

      //打通情况下的storage

      //创建abtest bridge实例
      this.abBridge = new this.SensorsABTest.sd.JSBridge({
        type: 'abtest',
        app_call_js: function (data) {
          try {
            data = _.base64Decode(data);
          } catch (error) {
            _this.SensorsABTest.log('App数据base64解码异常', data);
          }
          try {
            data = JSON.parse(data);
            if (data.message_id) {
              // 调用sd.JSBridge.double
              this.double(data);
            }
          } catch (error) {
            _this.SensorsABTest.log('App数据解析异常', data);
          }
        }
      });

      //判断App abtest是否可用
      if (_.isObject(window.SensorsData_iOS_JS_Bridge) && window.SensorsData_iOS_JS_Bridge.sensorsdata_abtest_module && this.abBridge.hasAppBridge()) {
        this.SensorsABTest.bridgeState = 'ab_bridge_ok';
      } else if (_.isObject(window.SensorsData_APP_New_H5_Bridge) && _.isFunction(window.SensorsData_APP_New_H5_Bridge.sensorsdata_abtest_module) && window.SensorsData_APP_New_H5_Bridge.sensorsdata_abtest_module() && this.abBridge.hasAppBridge()) {
        this.SensorsABTest.bridgeState = 'ab_bridge_ok';
      } else {
        this.SensorsABTest.bridgeState = 'ab_no_abtest_bridge';
      }

      this.SensorsABTest.store.init(this.getResultFromApp, this);
    },
    //设置全局参数，打通H5目前仅限制timeout
    setPara: function (para) {
      var verObj = this.SensorsABTest.verifyStore.para('打通初始化', para, {
        timeout_milliseconds: 'not_essential'
      });
      this.SensorsABTest.para = _.extend({}, this.SensorsABTest.default_para, verObj.para);
      if (!_.isBoolean(this.SensorsABTest.para.collect_bridge_status)) {
        this.SensorsABTest.para.collect_bridge_status = true;
      }
      if (!_.isNumber(this.SensorsABTest.para.update_interval)) {
        this.SensorsABTest.para.update_interval = 600000;
      }
      if (this.SensorsABTest.sd.para.encrypt_cookie === true) {
        this.SensorsABTest.para.encrypt_cookie = true;
      }
      if (this.SensorsABTest.sd.para.sdk_id) {
        this.SensorsABTest.state.storage.name += '_' + this.SensorsABTest.sd.para.sdk_id;
      }
      return true;
    },
    getResultFromApp: function (obj) {
      var _this = this;
      obj = _.isObject(obj) ? obj : {};
      var para = obj.para || {};
      var suc = obj.suc;
      var err = obj.err;
      var time_out = para.timeout_milliseconds || this.SensorsABTest.para.timeout_milliseconds;
      var request_body = {
        origin_platform: 'H5'
      };
      if (_.isObject(para.custom_properties)) {
        request_body = _.extend(request_body, {
          custom_properties: para.custom_properties,
          param_name: para.param_name
        });
      }
      function sendRequest() {
        if (_this.SensorsABTest.bridgeState === 'ab_bridge_ok') {
          _this.abBridge.requestToApp({
            data: {
              properties: para.properties,
              timeout: time_out,
              request_body: request_body
            },
            callback: function (data) {
              if (_.isObject(data) && _.isObject(data.data)) {
                _this.SensorsABTest.log('成功获取到 App 端返回的试验数据', 'data:', data);
                _this.SensorsABTest.dealResponseData(data.data);
                if (suc) {
                  suc(data);
                }
              } else {
                //App端网络异常或者url错误等导致的失败
                _this.SensorsABTest.log('App 端请求失败');
                if (err) {
                  err();
                }
              }
              _this.SensorsABTest.fetchData.setNextFetch();
            },
            timeout: {
              time: time_out,
              callback: function () {
                _this.SensorsABTest.log('获取App端数据失败');
                if (err) {
                  err();
                }
                _this.SensorsABTest.fetchData.setNextFetch();
              }
            }
          });
        } else {
          if (err) {
            _this.SensorsABTest.log('A/B Testing 打通失败，', _this.SensorsABTest.bridgeState);
            err();
          }
        }
      }
      this.SensorsABTest.log('向App发起试验请求');
      sendRequest();
    },
    asyncFetch: function (para) {
      var _this = this;
      this.SensorsABTest.bridgeStore.getResultFromApp({
        para: para,
        suc: function (data) {
          if (_.isObject(data.properties)) {
            para.properties = _.extend(data.properties, para.properties);
          }
          if (data.data.status === 'SUCCESS') {
            var result = _this.SensorsABTest.getExpResult(para);
            para.callback(result);
          } else {
            para.callback(para.default_value);
          }
        },
        err: function () {
          para.callback(para.default_value);
        }
      });
    }
  };

  BridgeStore.prototype.methods = {
    asyncFetchABTest: function (para) {
      if (!_.isObject(para)) {
        this.SensorsABTest.log('asyncFetchABTest调用失败，参数未正确配置');
        return false;
      }
      var verifyobj = this.SensorsABTest.verifyStore.para('asyncFetchABTest', para, {
        param_name: 'essential',
        value_type: 'essential',
        default_value: 'essential',
        callback: 'essential',
        timeout_milliseconds: 'not_essential',
        properties: 'not_essential'
      });

      //参数校验没通过，方法不能调用
      if (!verifyobj.verify_success) {
        return;
      }

      para = verifyobj.para;

      var verifyCustom = this.SensorsABTest.verifyStore.resolveCustomProperties(para);

      //参数校验没通过，返回默认值
      if (!verifyCustom.verify_success) {
        para.callback(para.default_value);
        return;
      }

      para = verifyCustom.para;

      if (this.SensorsABTest.bridgeState !== 'ab_bridge_ok') {
        para.callback(para.default_value);
        return;
      }

      this.SensorsABTest.bridgeStore.asyncFetch(para);
    },
    fastFetchABTest: function (para) {
      if (!_.isObject(para)) {
        this.SensorsABTest.log('fastFetchABTest调用失败，参数未正确配置');
        return false;
      }
      var verifyobj = this.SensorsABTest.verifyStore.para('fastFetchABTest', para, {
        param_name: 'essential',
        value_type: 'essential',
        default_value: 'essential',
        callback: 'essential',
        timeout_milliseconds: 'not_essential',
        properties: 'not_essential'
      });

      //参数校验没通过，方法不能调用
      if (!verifyobj.verify_success) {
        return;
      }

      para = verifyobj.para;

      //先从本地获取，本地取不到再发起请求
      var expObj = this.SensorsABTest.searchLocalExp(para.param_name);
      if (_.isObject(expObj)) {
        var result = this.SensorsABTest.getExpResult(para, expObj);
        para.callback(result);
        return;
      }

      var verifyCustom = this.SensorsABTest.verifyStore.resolveCustomProperties(para);

      //参数校验没通过，返回默认值
      if (!verifyCustom.verify_success) {
        para.callback(para.default_value);
        return;
      }

      para = verifyCustom.para;

      if (this.SensorsABTest.bridgeState !== 'ab_bridge_ok') {
        para.callback(para.default_value);
        return;
      }

      this.SensorsABTest.log('fastFetchABTest缓存中未读取到数据，发起请求');
      this.SensorsABTest.bridgeStore.asyncFetch(para);
    },
    //从缓存获取试验数据
    fetchCacheABTest: function (para) {
      if (!_.isObject(para)) {
        this.SensorsABTest.log('fetchCacheABTest调用失败，参数未正确配置');
        return;
      }
      var verifyobj = this.SensorsABTest.verifyStore.para('fetchCacheABTest', para, {
        param_name: 'essential',
        value_type: 'essential',
        default_value: 'essential'
      });

      //参数校验没通过，方法不能调用
      if (!verifyobj.verify_success) {
        return;
      }
      return this.SensorsABTest.getExpResult(verifyobj.para);
    }
  };

  // 字符映射加密标记
  var flag_dfm = 'dfm-enc-';
  // 自创的 df-mapping- 加解密 映射加密
  function dfmapping(option) {
    var str = 't6KJCZa5pDdQ9khoEM3Tj70fbP2eLSyc4BrsYugARqFIw1mzlGNVXOHiWvxUn8';
    var len = str.length - 1;
    var relation = {};
    var i = 0;
    for (i = 0; i < str.length; i++) {
      relation[str.charAt(i)] = str.charAt(len - i);
    }
    var newStr = '';
    for (i = 0; i < option.length; i++) {
      if (option.charAt(i) in relation) {
        newStr += relation[option.charAt(i)];
      } else {
        newStr += option.charAt(i);
      }
    }
    return newStr;
  }

  /**
   * 解密  ab 及 弹窗直接使用字符映射
   *
   * @export
   * @param { String } v
   * @return { String }
   */
  function decrypt(v) {
    if (v.indexOf(flag_dfm) === 0) {
      v = v.substring(flag_dfm.length);
      v = dfmapping(v);
    }
    return v;
  }

  /**
   * 加密
   *
   * @export
   * @param { String } v
   * @return { String }
   */
  function encrypt(v) {
    return flag_dfm + dfmapping(v);
  }

  function VerifyStore(SensorsABTest) {
    this.SensorsABTest = SensorsABTest;
  }
  //参数校验
  VerifyStore.prototype = {
    value_type_list: ['Number', 'String', 'Object', 'Boolean'],
    regName: /^((?!^distinct_id$|^original_id$|^time$|^properties$|^id$|^first_id$|^second_id$|^users$|^events$|^event$|^user_id$|^date$|^datetime$|^user_tag.*|^user_group.*)[a-zA-Z_][a-zA-Z\d_]*)$/i,
    valueType: function (value, type) {
      switch (type) {
      case 'Number':
        if (_.isNumber(value)) {
          return true;
        }
        break;
      case 'String':
        if (_.isString(value)) {
          return true;
        }
        break;
      case 'Object':
        if (_.isObject(value)) {
          return true;
        }
        break;
      case 'Boolean':
        if (value === true || value === false) {
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
    para: function (name, para, obj) {
      var _this = this;
      var result = {
        verify_success: true,
        para: null
      };

      _.each(obj, function (value, prop) {
        if (value === 'essential') {
          switch (prop) {
          case 'param_name':
            if (!(_.isString(para.param_name) && para.param_name.length > 0)) {
              _this.SensorsABTest.log(name + '方法调用失败，param_name参数未正确配置！param_name:', para.param_name);
              result.verify_success = false;
            }
            break;
          case 'value_type':
            if (!(_.isString(para.value_type) && _.indexOf(_this.value_type_list, para.value_type) !== -1)) {
              _this.SensorsABTest.log(name + '方法调用失败，value_type配置错误', para.value_type);
              result.verify_success = false;
            }
            break;
          case 'default_value':
            if (typeof para.default_value === 'undefined') {
              _this.SensorsABTest.log(name + '方法调用失败，default_value参数未配置');
              result.verify_success = false;
            } else if (!_this.valueType(para.default_value, para.value_type)) {
              _this.SensorsABTest.log(name + '方法调用失败，default_value类型必须与value_type一致！', para.default_value, para.value_type);
              result.verify_success = false;
            }
            break;
          case 'callback':
            if (!_.isFunction(para.callback)) {
              _this.SensorsABTest.log(name + '方法调用失败，callback参数未正确配置');
              result.verify_success = false;
            }
            break;
          default:
            result.verify_success = false;
            break;
          }
        } else if (value === 'not_essential') {
          switch (prop) {
          case 'timeout_milliseconds':
            para.timeout_milliseconds = para.timeout_milliseconds || _this.SensorsABTest.para.timeout_milliseconds || _this.SensorsABTest.default_para.timeout_milliseconds;
            if (!_.isNumber(para.timeout_milliseconds) || (_.isNumber(para.timeout_milliseconds) && para.timeout_milliseconds <= 0)) {
              _this.SensorsABTest.log('timeout_milliseconds 参数错误', para.timeout_milliseconds);
              para.timeout_milliseconds = _this.SensorsABTest.para.timeout_milliseconds;
            }
            if (para.timeout_milliseconds < 200) {
              para.timeout_milliseconds = 200;
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
    },
    resolveCustomProperties: function (para) {
      var _this = this;
      var result = {
        verify_success: true,
        para: null
      };
      var custom_properties = para.custom_properties;

      if (!_.isObject(custom_properties) || _.isEmptyObject(custom_properties)) {
        delete para.custom_properties;
        result.para = para;
        return result;
      }

      _.each(custom_properties, function (val, key) {
        if (!_.isString(key) || !_this.regName.test(key) || key.length > 100) {
          _this.SensorsABTest.log(' property name [ ' + key + ' ] is not invalid ');
          result.verify_success = false;
        }

        if ((!_.isString(val) && !_.isNumber(val) && !_.isBoolean(val) && !_.isArray(val) && !_.isDate(val)) || (_.isString(val) && val.length > 500)) {
          _this.SensorsABTest.log('property [ ' + key + ' ] of value [ ' + JSON.stringify(val) + ' ] is not invalid');
          result.verify_success = false;
        }

        if (_.isArray(val)) {
          var verifyResult = true;
          _.each(val, function (item) {
            if (verifyResult === false) return;
            if (!_.isString(item)) {
              verifyResult = false;
            }
          });
          if (!verifyResult) {
            _this.SensorsABTest.log('property  value type can be array, but only allow string item. property [ ' + key + ' ] of value  ' + JSON.stringify(val) + '  is not invalid');
            result.verify_success = false;
          }
        }
      });
      if (result.verify_success === true) {
        var custom_para = {};
        _.each(custom_properties, function (val, key) {
          if (_.isDate(val)) {
            custom_para[key] = _.formatDate(val);
          } else if (!_.isString(val)) {
            custom_para[key] = JSON.stringify(val);
          } else {
            custom_para[key] = val;
          }
        });
        para.custom_properties = custom_para;
      }
      result.para = para;
      return result;
    }
  };
  //sa必须初始化
  function getSA(sd) {
    if (_.isObject(sd) && _.isObject(sd.readyState) && sd.readyState.state >= 3) {
      return sd;
    } else {
      return null;
    }
  }

  function listenPageState(obj) {
    var visibilystore = {
      visibleHandle: _.isFunction(obj.visible) ? obj.visible : function () {},
      hiddenHandler: _.isFunction(obj.hidden) ? obj.hidden : function () {},
      visibilityChange: null,
      hidden: null,
      isSupport: function () {
        return typeof document[this.hidden] !== 'undefined';
      },
      init: function () {
        if (typeof document.hidden !== 'undefined') {
          this.hidden = 'hidden';
          this.visibilityChange = 'visibilitychange';
        } else if (typeof document.mozHidden !== 'undefined') {
          this.hidden = 'mozHidden';
          this.visibilityChange = 'mozvisibilitychange';
        } else if (typeof document.msHidden !== 'undefined') {
          this.hidden = 'msHidden';
          this.visibilityChange = 'msvisibilitychange';
        } else if (typeof document.webkitHidden !== 'undefined') {
          this.hidden = 'webkitHidden';
          this.visibilityChange = 'webkitvisibilitychange';
        }
        this.listen();
      },
      listen: function () {
        if (!this.isSupport()) {
          if (document.addEventListener) {
            /*for browsers without focusin/out support eg. firefox, opera use focus/blur*/
            /*window used instead of doc as Opera complains otherwise*/
            window.addEventListener('focus', this.visibleHandle, 1);
            window.addEventListener('blur', this.hiddenHandler, 1);
          } else {
            /*IE <10s most reliable focus events are onfocusin/onfocusout*/
            document.attachEvent('onfocusin', this.visibleHandle);
            document.attachEvent('onfocusout', this.hiddenHandler);
          }
        } else {
          var _this = this;
          document.addEventListener(
            _this.visibilityChange,
            function () {
              if (!document[_this.hidden]) {
                _this.visibleHandle();
              } else {
                _this.hiddenHandler();
              }
            },
            1
          );
        }
      }
    };
    visibilystore.init();
  }

  function getStorageData(name) {
    var data = _.storage.get(name);
    if (_.isString(data)) {
      data = decrypt(data);
    }
    try {
      data = JSON.parse(data);
    } catch (e) {
      _.log(e);
    }
    return data;
  }
  function setStorageData(data, name, encrypt_cookie) {
    data = JSON.stringify(data);
    if (encrypt_cookie) {
      data = encrypt(data);
    }
    _.storage.set(data, name);
  }

  function Mask(attr_suffix) {
    this.attr_name = 'sa-abtest-' + attr_suffix;
    this.remove_timer = null;
    this.is_added = false;
    this.is_abort = false;
    this.is_timeout = false;
    this.style_element = null;
    this.createMaskStyle();
  }

  Mask.prototype = {
    createMaskStyle: function () {
      var css = '[' + this.attr_name + '],[' + this.attr_name + '] body{opacity:0 !important;-khtml-opacity:0 !important;-moz-opacity:0;filter:alpha(opacity=0);}';
      var style = document.createElement('style');
      style.type = 'text/css';
      try {
        style.appendChild(document.createTextNode(css));
      } catch (e) {
        style.styleSheet.cssText = css;
      }
      try {
        this.style_element = style;
        document.getElementsByTagName('head')[0].appendChild(style);
      } catch (e) {
        _.log('error when create calls');
      }
    },
    /**
     * @param {*} timeout 遮罩层去除时间
     */
    show: function (timeout) {
      try {
        var _this = this;
        if (this.is_abort) {
          return false;
        }
        var html = null;
        html = document.getElementsByTagName('html')[0];

        if (!_.isElement(html)) {
          return;
        }
        html.setAttribute(this.attr_name, '1');
        this.is_added = true;
        if (_.isNumber(timeout)) {
          this.remove_timer = setTimeout(function () {
            _this.is_timeout = true;
            _this.remove();
          }, timeout);
        }
      } catch (e) {
        _.log('error when show calls');
      }
    },
    //移除遮罩层
    remove: function () {
      try {
        if (!this.is_abort) {
          this.is_abort = true;
        }
        if (this.is_added) {
          this.is_added = false;
          var html = null;

          html = document.getElementsByTagName('html')[0];

          if (!_.isElement(html)) {
            return;
          }
          html.removeAttribute(this.attr_name);
          if (this.style_element) {
            document.getElementsByTagName('head')[0].removeChild(this.style_element);
            this.style_element = null;
          }
        }
        if (this.remove_timer) {
          clearTimeout(this.remove_timer);
          this.remove_timer = null;
        }
      } catch (e) {
        _.log('error when remove calls');
      }
    }
  };

  /**
   * 多链接试验
   */

  function Link(SensorsABTest) {
    this.SensorsABTest = SensorsABTest;
    // 首次命中试验
    this.isFirst = true;
    // 初始化多链试验时间
    this.start_time = null;
    // 遮罩实例
    this.mask_instance = null;
    //页面被筛除不做试验
    this.is_filtered_out = false;
    // 多链试验配置
    this.para = {
      timeout: 500,
      use_mask: true,
      pass_params: true,
      control_link_search: 'default',
      experiment_link_search: 'default'
    };
  }
  /**
   * 1、初始化多链接参数
   * 2、增加遮罩层
   * 3、记录初始时间
   */
  Link.prototype.init = function (linkPara) {
    this.initPara(linkPara);
    if (!this.para) {
      this.SensorsABTest.log('multilink is closed');
      return false;
    }

    if (_.getQueryParam(location.href, 'saSDKMultilink')) {
      this.is_filtered_out = true;
      return false;
    }

    this.start_time = new Date().getTime();
    if (this.para.use_mask) {
      // 创建遮罩层
      this.initMask();
    }
    this.addSinglePageListener();
  };
  /**
   * 单页面跳转重置状态，判断是否进入多链接试验
   */
  Link.prototype.addSinglePageListener = function () {
    var _this = this;
    _.addSinglePageEvent(function (last_url) {
      if (last_url === location.href) {
        return false;
      }
      _this.is_filtered_out = false;
      _this.isFirst = true;
      if (_this.mask_instance) {
        _this.mask_instance.remove();
      }
      if (_.getQueryParam(location.href, 'saSDKMultilink')) {
        _this.is_filtered_out = true;
        return false;
      }
      _this.start_time = new Date().getTime();
      if (_this.para.use_mask) {
        _this.initMask();
      }
      _this.resolve();
    });
  };

  Link.prototype.initMask = function () {
    this.mask_instance = new Mask('link' + '_' + this.SensorsABTest.sd.para.sdk_id);
    this.mask_instance.show(this.para.timeout);
  };

  Link.prototype.initPara = function (linkPara) {
    if (_.isFunction(linkPara)) {
      try {
        linkPara = linkPara();
      } catch (error) {
        this.SensorsABTest.log('link para error!');
        this.para = false;
        return;
      }
    }
    if (linkPara === false) {
      this.para = false;
    } else if (_.isObject(linkPara)) {
      this.para.timeout = _.isNumber(linkPara.timeout) && linkPara.timeout >= 0 ? linkPara.timeout : 500;
      this.para.use_mask = _.isBoolean(linkPara.use_mask) ? linkPara.use_mask : this.para.use_mask;
      this.para.control_link_search = _.isString(linkPara.control_link_search) ? linkPara.control_link_search : 'default';
      this.para.experiment_link_search = _.isString(linkPara.experiment_link_search) ? linkPara.experiment_link_search : 'default';
      this.para.pass_params = _.isBoolean(linkPara.pass_params) ? linkPara.pass_params : true;
      this.para.onRedirect = _.isFunction(linkPara.onRedirect) ? linkPara.onRedirect : null;
    }
  };

  Link.prototype.resolve = function () {
    var result = this.SensorsABTest.results;
    var _this = this;
    if (!this.para) {
      return false;
    }
    if (!_.isArray(result)) {
      return false;
    }
    if (this.is_filtered_out) {
      return false;
    }
    if (!this.isFirst) {
      return false;
    }
    this.isFirst = false;

    if (this.para.timeout === 0) {
      return false;
    }

    if (new Date().getTime() - this.start_time > this.para.timeout) {
      this.SensorsABTest.log('The multilink was stopped because the request timeout');
      return false;
    }
    var is_trigger = false;

    _.each(result, function (exp) {
      if (!_.isObject(exp)) {
        return false;
      }

      if (is_trigger) {
        return false;
      }

      if (exp.experiment_type !== 'LINK') {
        return false;
      }

      if (_this.isTriggerLinkExp(exp)) {
        is_trigger = true;

        _this.redirectUrl(exp);
      }
    });

    if (!is_trigger && this.para.use_mask && this.mask_instance) {
      this.mask_instance.remove();
    }
  };

  /**
   * 解析数据流程
   * 1、校验是否命中多链接试验
   * 2、去除遮罩层
   */
  Link.prototype.stopTrigger = function () {
    this.isFirst = false;
    this.SensorsABTest.log('The multilink was stopped because the request failed');
    if (this.para.use_mask && this.mask_instance) {
      this.mask_instance.remove();
    }
  };

  /**
   * 检查是否命中多链接试验
   * @param {*} exp
   */
  Link.prototype.isTriggerLinkExp = function (exp) {
    if (!(_.isString(exp.control_link) && _.isString(exp.link_match_type))) {
      this.SensorsABTest.log('多链接试验数据异常', exp.abtest_experiment_id);
      return false;
    }
    // 多链实验正则匹配
    if (exp.link_match_type === 'REGEXP') {
      if (_.checkUrlIsRegexp(exp.control_link, exp.regexp_flags)) {
        return true;
      }
      this.SensorsABTest.log('多链接试验匹配失败', exp.abtest_experiment_id);
      return false;
    }
    // 多链实验非正则匹配
    if (_.checkUrlIsMatch(exp.control_link, exp.link_match_type)) {
      return true;
    } else {
      this.SensorsABTest.log('多链接试验匹配失败', exp.abtest_experiment_id);
      return false;
    }
  };

  //跳转到experiment_link
  Link.prototype.redirectUrl = function (exp) {
    if (!(exp.experiment_link && _.isString(exp.experiment_link))) {
      this.SensorsABTest.log('页面跳转失败，experiment_link字段异常', exp.abtest_experiment_id);
      return;
    }
    if (exp.is_control_group === true && (exp.link_match_type === 'STRICT' || exp.link_match_type === 'REGEXP')) {
      this.SensorsABTest.triggerHandle(exp); // 严格匹配、对照组正则匹配不跳转时，发送 $ABTestTrigger
      return; //对照组严格匹配不跳转，对照组正则匹配不跳转
    }
    function validUrl(value) {
      if (_.secCheck.isHttpUrl(value)) {
        return _.secCheck.removeScriptProtocol(value);
      } else {
        this.SensorsABTest.log('非法URL');
        return false;
      }
    }

    var page_url = location.href;
    var experiment_link = exp.experiment_link;
    // 试验 Url 包含 $1 $2... 需要正则匹配
    // 不包含则不进行匹配，直接使用实验 Url
    var hasRegExp = /\$\d+/;
    if (exp.link_match_type === 'REGEXP' && hasRegExp.test(experiment_link)) {
      var link_regexp = exp.regexp_flags ? new RegExp(exp.control_link, exp.regexp_flags) : new RegExp(exp.control_link);
      experiment_link = page_url.replace(link_regexp, experiment_link);
    }
    var valid_url, result;
    if (this.para.pass_params) {
      result = this.getMergedUrl(exp.is_control_group, experiment_link, page_url);
      valid_url = validUrl(result);
    } else {
      result = this.getMergedUrl(exp.is_control_group, experiment_link);
      valid_url = validUrl(result);
    }
    if (valid_url) {
      var isTrack = this.SensorsABTest.triggerHandle(exp); //$ABTestTrigger
      var timeout = isTrack ? 80 : 0;

      if (this.para.onRedirect) {
        this.para.onRedirect(valid_url);
        // 用户自己处理跳转链接，无法获知跳转时间点。未防止白屏时间过长。立即清除遮罩
        this.mask_instance.remove();
      } else {
        this.redirect(valid_url, timeout);
      }

      if (this.mask_instance && this.mask_instance.remove_timer) {
        clearTimeout(this.mask_instance.remove_timer);
      }
    }
  };

  Link.prototype.redirect = function (url, timeout) {
    setTimeout(function () {
      location.href = url; //延迟跳转，降低关闭页面前发数据风险
    }, timeout);
  };
  /**
   * 获取组合好的url
   * 1、解析原始原始页面 url 上的参数和hash
   *      a.根据 control_link_search  确定参数从哪解析
   * 2、解析试验页面地址 link 上的参数和hash
   *      a.根据 control_link_search（对照组）或 experiment_link_search（试验组）确定目标url从哪解析
   * 3、合并参数和 hash，拼接在 link 上
   *      a.根据 experiment_link_search（试验组）来确定参数拼接到那个位置
   * 4、相同的参数和 hash 使用试验地址 link 的
   * @param {*} link 目标url
   * @param {*} url  参数url
   * @returns url
   */
  Link.prototype.getMergedUrl = function (is_control_group, link, url) {
    var result = '';
    var search_str = '';
    var hash = '';
    var urlParse = {
      parse_url: null,
      search: '' //按照配置解析后的search
    };
    var linkParse = {
      parse_url: null,
      search: '', //按照配置解析后的search
      hash: '' //按照配置拆解的#部分
    };
    function parseUrl(url) {
      var reg = /([^?#]+)(\?[^#]*)?(#.*)?/;
      var arr = reg.exec(url);
      if (!arr) {
        return;
      }
      var host = arr[1] || '',
        search = arr[2] || '',
        hash = arr[3] || '';
      return {
        host: host,
        search: search,
        hash: hash
      };
    }
    if (url) {
      urlParse.parse_url = parseUrl(url);
      if (!urlParse.parse_url) {
        this.SensorsABTest.log('url 解析失败', url);
        return;
      }
    }

    linkParse.parse_url = parseUrl(link);
    if (!linkParse.parse_url) {
      this.SensorsABTest.log('url 解析失败', link);
      return;
    }

    if (url) {
      //按照规则解析页面url search
      if (this.para.control_link_search === 'after_hash') {
        urlParse.search = resolveHash(urlParse.parse_url.hash).search;
      } else {
        urlParse.search = urlParse.parse_url.search;
      }
    }

    //解析跳转目标url的 search
    if (is_control_group) {
      if (this.para.control_link_search === 'after_hash') {
        linkParse.search = resolveHash(linkParse.parse_url.hash).search;
        linkParse.hash = resolveHash(linkParse.parse_url.hash).hash;
      } else {
        linkParse.search = linkParse.parse_url.search;
      }
    } else {
      if (this.para.experiment_link_search === 'after_hash') {
        linkParse.search = resolveHash(linkParse.parse_url.hash).search;
        linkParse.hash = resolveHash(linkParse.parse_url.hash).hash;
      } else {
        linkParse.search = linkParse.parse_url.search;
      }
    }

    function resolveHash(hash) {
      var obj = {
        hash: '',
        search: ''
      };
      if (!_.isString(hash)) {
        return obj;
      }
      var index = hash.indexOf('?');
      if (index > -1) {
        obj.search = hash.slice(index);
        obj.hash = hash.slice(0, index);
      } else {
        obj.hash = hash;
      }
      return obj;
    }

    if (url) {
      //拼接
      search_str = this.getSearchStr(urlParse.search, linkParse.search);
    } else {
      search_str = this.getSearchStr(linkParse.search);
    }

    if ((is_control_group && this.para.control_link_search === 'after_hash') || (!is_control_group && this.para.experiment_link_search === 'after_hash')) {
      //目标 url 如果拼接方式是 afterhash ，先#再？,只拼接search，如果有 # 拼接在#之后的？后。没#拼接在url后
      if (linkParse.hash.length > 0) {
        result = linkParse.parse_url.host + linkParse.parse_url.search + linkParse.hash + search_str;
      } else {
        if (linkParse.parse_url.search.length > 0) {
          result = linkParse.parse_url.host + linkParse.parse_url.search + '&' + search_str.substring(1);
        } else {
          result = linkParse.parse_url.host + search_str;
        }
      }
    } else {
      //目标url按照 ？ # 顺序拼接
      if (this.para.control_link_search === 'after_hash') {
        //合并hash时，如果原始url是#? 那么就不用原始页面的hash
        hash = linkParse.parse_url.hash;
      } else {
        if (url) {
          hash = linkParse.parse_url.hash || urlParse.parse_url.hash;
        } else {
          hash = linkParse.parse_url.hash;
        }
      }

      result = linkParse.parse_url.host + search_str + hash;
    }
    return result;
  };
  /**
   * 合并search,加 multilink 标志，heavyStr优先级高
   * @param {*} baseStr
   * @param {*} heavyStr
   * return ?source=exp&saSDKMultilink=true
   */
  Link.prototype.getSearchStr = function (baseStr, heavyStr) {
    function getParams(search) {
      var obj = {};
      if (!search || !search.length || search.indexOf('?') !== 0) {
        return obj;
      }
      search = search.slice(1);
      if (!search.length) {
        return obj;
      }
      var arr = search.split('&');
      for (var i = 0; i < arr.length; i++) {
        if (arr[i].indexOf('=') < 0) {
          obj[arr[i]] = null;
        } else {
          var param_arr = arr[i].split('=');
          obj[param_arr[0]] = param_arr[1];
        }
      }
      return obj;
    }
    var baseObj = getParams(baseStr);
    var heavyObj = getParams(heavyStr);
    var resultObj = _.extend(baseObj, heavyObj);
    var str = '';
    if (_.isEmptyObject(resultObj)) {
      return '?saSDKMultilink=true';
    } else {
      var isFirst = true;
      for (var prop in resultObj) {
        if (isFirst) {
          str += '?';
          isFirst = false;
        } else {
          str += '&';
        }
        if (resultObj[prop] !== null) {
          str = str + prop + '=' + resultObj[prop];
        } else {
          str += prop;
        }
      }
      return (str += '&saSDKMultilink=true');
    }
  };

  /*!
   * Shim for MutationObserver interface
   * Author: Graeme Yeates (github.com/megawac)
   * Repository: https://github.com/megawac/MutationObserver.js
   * License: WTFPL V2, 2004 (wtfpl.net).
   * Though credit and staring the repo will make me feel pretty, you can modify and redistribute as you please.
   * Attempts to follow spec (https://www.w3.org/TR/dom/#mutation-observers) as closely as possible for native javascript
   * See https://github.com/WebKit/webkit/blob/master/Source/WebCore/dom/MutationObserver.cpp for current webkit source c++ implementation
   */

  /**
   * prefix bugs:
      - https://bugs.webkit.org/show_bug.cgi?id=85161
      - https://bugzilla.mozilla.org/show_bug.cgi?id=749920
   * Don't use WebKitMutationObserver as Safari (6.0.5-6.1) use a buggy implementation
  */
  if (!window.MutationObserver) {
    window.MutationObserver = (function (undefined$1) {
      /**
       * @param {function(Array.<MutationRecord>, MutationObserver)} listener
       * @constructor
       */
      function MutationObserver(listener) {
        /**
         * @type {Array.<Object>}
         * @private
         */
        this._watched = [];
        /** @private */
        this._listener = listener;
      }

      /**
       * Start a recursive timeout function to check all items being observed for mutations
       * @type {MutationObserver} observer
       * @private
       */
      function startMutationChecker(observer) {
        (function check() {
          var mutations = observer.takeRecords();

          if (mutations.length) {
            // fire away
            // calling the listener with context is not spec but currently consistent with FF and WebKit
            observer._listener(mutations, observer);
          }
          /** @private */
          observer._timeout = setTimeout(check, MutationObserver._period);
        })();
      }

      /**
       * Period to check for mutations (~32 times/sec)
       * @type {number}
       * @expose
       */
      MutationObserver._period = 30 /*ms+runtime*/;

      /**
       * Exposed API
       * @expose
       * @final
       */
      MutationObserver.prototype = {
        /**
         * see https://dom.spec.whatwg.org/#dom-mutationobserver-observe
         * not going to throw here but going to follow the current spec config sets
         * @param {Node|null} $target
         * @param {Object|null} config : MutationObserverInit configuration dictionary
         * @expose
         * @return undefined
         */
        observe: function ($target, config) {
          /**
           * Using slightly different names so closure can go ham
           * @type {!Object} : A custom mutation config
           */
          var settings = {
            attr: !!(config.attributes || config.attributeFilter || config.attributeOldValue),

            // some browsers enforce that subtree must be set with childList, attributes or characterData.
            // We don't care as spec doesn't specify this rule.
            kids: !!config.childList,
            descendents: !!config.subtree,
            charData: !!(config.characterData || config.characterDataOldValue)
          };

          var watched = this._watched;

          // remove already observed target element from pool
          for (var i = 0; i < watched.length; i++) {
            if (watched[i].tar === $target) watched.splice(i, 1);
          }

          if (config.attributeFilter) {
            /**
             * converts to a {key: true} dict for faster lookup
             * @type {Object.<String,Boolean>}
             */
            settings.afilter = reduce(
              config.attributeFilter,
              function (a, b) {
                a[b] = true;
                return a;
              },
              {}
            );
          }

          watched.push({
            tar: $target,
            fn: createMutationSearcher($target, settings)
          });

          // reconnect if not connected
          if (!this._timeout) {
            startMutationChecker(this);
          }
        },

        /**
         * Finds mutations since last check and empties the "record queue" i.e. mutations will only be found once
         * @expose
         * @return {Array.<MutationRecord>}
         */
        takeRecords: function () {
          var mutations = [];
          var watched = this._watched;

          for (var i = 0; i < watched.length; i++) {
            watched[i].fn(mutations);
          }

          return mutations;
        },

        /**
         * @expose
         * @return undefined
         */
        disconnect: function () {
          this._watched = []; // clear the stuff being observed
          clearTimeout(this._timeout); // ready for garbage collection
          /** @private */
          this._timeout = null;
        }
      };

      /**
       * Simple MutationRecord pseudoclass. No longer exposing as its not fully compliant
       * @param {Object} data
       * @return {Object} a MutationRecord
       */
      function MutationRecord(data) {
        var settings = {
          // technically these should be on proto so hasOwnProperty will return false for non explicitly props
          type: null,
          target: null,
          addedNodes: [],
          removedNodes: [],
          previousSibling: null,
          nextSibling: null,
          attributeName: null,
          attributeNamespace: null,
          oldValue: null
        };
        for (var prop in data) {
          if (has(settings, prop) && data[prop] !== undefined$1) settings[prop] = data[prop];
        }
        return settings;
      }

      /**
       * Creates a func to find all the mutations
       *
       * @param {Node} $target
       * @param {!Object} config : A custom mutation config
       */
      function createMutationSearcher($target, config) {
        /** type {Elestuct} */
        var $oldstate = clone($target, config); // create the cloned datastructure

        /**
         * consumes array of mutations we can push to
         *
         * @param {Array.<MutationRecord>} mutations
         */
        return function (mutations) {
          var olen = mutations.length,
            dirty;

          if (config.charData && $target.nodeType === 3 && $target.nodeValue !== $oldstate.charData) {
            mutations.push(
              new MutationRecord({
                type: 'characterData',
                target: $target,
                oldValue: $oldstate.charData
              })
            );
          }

          // Alright we check base level changes in attributes... easy
          if (config.attr && $oldstate.attr) {
            findAttributeMutations(mutations, $target, $oldstate.attr, config.afilter);
          }

          // check childlist or subtree for mutations
          if (config.kids || config.descendents) {
            dirty = searchSubtree(mutations, $target, $oldstate, config);
          }

          // reclone data structure if theres changes
          if (dirty || mutations.length !== olen) {
            /** type {Elestuct} */
            $oldstate = clone($target, config);
          }
        };
      }

      /* attributes + attributeFilter helpers */

      // Check if the environment has the attribute bug (#4) which cause
      // element.attributes.style to always be null.
      var hasAttributeBug = document.createElement('i');
      hasAttributeBug.style.top = 0;
      hasAttributeBug = hasAttributeBug.attributes.style.value != 'null';

      /**
       * Gets an attribute value in an environment without attribute bug
       *
       * @param {Node} el
       * @param {Attr} attr
       * @return {String} an attribute value
       */
      function getAttributeSimple(el, attr) {
        // There is a potential for a warning to occur here if the attribute is a
        // custom attribute in IE<9 with a custom .toString() method. This is
        // just a warning and doesn't affect execution (see #21)
        return attr.value;
      }

      /**
       * Gets an attribute value with special hack for style attribute (see #4)
       *
       * @param {Node} el
       * @param {Attr} attr
       * @return {String} an attribute value
       */
      function getAttributeWithStyleHack(el, attr) {
        // As with getAttributeSimple there is a potential warning for custom attribtues in IE7.
        return attr.name !== 'style' ? attr.value : el.style.cssText;
      }

      var getAttributeValue = hasAttributeBug ? getAttributeSimple : getAttributeWithStyleHack;

      /**
       * fast helper to check to see if attributes object of an element has changed
       * doesnt handle the textnode case
       *
       * @param {Array.<MutationRecord>} mutations
       * @param {Node} $target
       * @param {Object.<string, string>} $oldstate : Custom attribute clone data structure from clone
       * @param {Object} filter
       */
      function findAttributeMutations(mutations, $target, $oldstate, filter) {
        var checked = {};
        var attributes = $target.attributes;
        var attr;
        var name;
        var i = attributes.length;
        while (i--) {
          attr = attributes[i];
          name = attr.name;
          if (!filter || has(filter, name)) {
            if (getAttributeValue($target, attr) !== $oldstate[name]) {
              // The pushing is redundant but gzips very nicely
              mutations.push(
                MutationRecord({
                  type: 'attributes',
                  target: $target,
                  attributeName: name,
                  oldValue: $oldstate[name],
                  attributeNamespace: attr.namespaceURI // in ie<8 it incorrectly will return undefined
                })
              );
            }
            checked[name] = true;
          }
        }
        for (name in $oldstate) {
          if (!checked[name]) {
            mutations.push(
              MutationRecord({
                target: $target,
                type: 'attributes',
                attributeName: name,
                oldValue: $oldstate[name]
              })
            );
          }
        }
      }

      /**
       * searchSubtree: array of mutations so far, element, element clone, bool
       * synchronous dfs comparision of two nodes
       * This function is applied to any observed element with childList or subtree specified
       * Sorry this is kind of confusing as shit, tried to comment it a bit...
       * codereview.stackexchange.com/questions/38351 discussion of an earlier version of this func
       *
       * @param {Array} mutations
       * @param {Node} $target
       * @param {!Object} $oldstate : A custom cloned node from clone()
       * @param {!Object} config : A custom mutation config
       */
      function searchSubtree(mutations, $target, $oldstate, config) {
        // Track if the tree is dirty and has to be recomputed (#14).
        var dirty;
        /*
         * Helper to identify node rearrangment and stuff...
         * There is no gaurentee that the same node will be identified for both added and removed nodes
         * if the positions have been shuffled.
         * conflicts array will be emptied by end of operation
         */
        function resolveConflicts(conflicts, node, $kids, $oldkids, numAddedNodes) {
          // the distance between the first conflicting node and the last
          var distance = conflicts.length - 1;
          // prevents same conflict being resolved twice consider when two nodes switch places.
          // only one should be given a mutation event (note -~ is used as a math.ceil shorthand)
          var counter = -~((distance - numAddedNodes) / 2);
          var $cur;
          var oldstruct;
          var conflict;
          while ((conflict = conflicts.pop())) {
            $cur = $kids[conflict.i];
            oldstruct = $oldkids[conflict.j];

            // attempt to determine if there was node rearrangement... won't gaurentee all matches
            // also handles case where added/removed nodes cause nodes to be identified as conflicts
            if (config.kids && counter && Math.abs(conflict.i - conflict.j) >= distance) {
              mutations.push(
                MutationRecord({
                  type: 'childList',
                  target: node,
                  addedNodes: [$cur],
                  removedNodes: [$cur],
                  // haha don't rely on this please
                  nextSibling: $cur.nextSibling,
                  previousSibling: $cur.previousSibling
                })
              );
              counter--; // found conflict
            }

            // Alright we found the resorted nodes now check for other types of mutations
            if (config.attr && oldstruct.attr) findAttributeMutations(mutations, $cur, oldstruct.attr, config.afilter);
            if (config.charData && $cur.nodeType === 3 && $cur.nodeValue !== oldstruct.charData) {
              mutations.push(
                MutationRecord({
                  type: 'characterData',
                  target: $cur,
                  oldValue: oldstruct.charData
                })
              );
            }
            // now look @ subtree
            if (config.descendents) findMutations($cur, oldstruct);
          }
        }

        /**
         * Main worker. Finds and adds mutations if there are any
         * @param {Node} node
         * @param {!Object} old : A cloned data structure using internal clone
         */
        function findMutations(node, old) {
          var $kids = node.childNodes;
          var $oldkids = old.kids;
          var klen = $kids.length;
          // $oldkids will be undefined for text and comment nodes
          var olen = $oldkids ? $oldkids.length : 0;
          // if (!olen && !klen) return; // both empty; clearly no changes

          // we delay the intialization of these for marginal performance in the expected case (actually quite signficant on large subtrees when these would be otherwise unused)
          // map of checked element of ids to prevent registering the same conflict twice
          var map;
          // array of potential conflicts (ie nodes that may have been re arranged)
          var conflicts;
          var id; // element id from getElementId helper
          var idx; // index of a moved or inserted element

          var oldstruct;
          // current and old nodes
          var $cur;
          var $old;
          // track the number of added nodes so we can resolve conflicts more accurately
          var numAddedNodes = 0;

          // iterate over both old and current child nodes at the same time
          var i = 0,
            j = 0;
          // while there is still anything left in $kids or $oldkids (same as i < $kids.length || j < $oldkids.length;)
          while (i < klen || j < olen) {
            // current and old nodes at the indexs
            $cur = $kids[i];
            oldstruct = $oldkids[j];
            $old = oldstruct && oldstruct.node;

            if ($cur === $old) {
              // expected case - optimized for this case
              // check attributes as specified by config
              if (config.attr && oldstruct.attr) /* oldstruct.attr instead of textnode check */ findAttributeMutations(mutations, $cur, oldstruct.attr, config.afilter);
              // check character data if node is a comment or textNode and it's being observed
              if (config.charData && oldstruct.charData !== undefined$1 && $cur.nodeValue !== oldstruct.charData) {
                mutations.push(
                  MutationRecord({
                    type: 'characterData',
                    target: $cur,
                    oldValue: oldstruct.charData
                  })
                );
              }

              // resolve conflicts; it will be undefined if there are no conflicts - otherwise an array
              if (conflicts) resolveConflicts(conflicts, node, $kids, $oldkids, numAddedNodes);

              // recurse on next level of children. Avoids the recursive call when there are no children left to iterate
              if (config.descendents && ($cur.childNodes.length || (oldstruct.kids && oldstruct.kids.length))) findMutations($cur, oldstruct);

              i++;
              j++;
            } else {
              // (uncommon case) lookahead until they are the same again or the end of children
              dirty = true;
              if (!map) {
                // delayed initalization (big perf benefit)
                map = {};
                conflicts = [];
              }
              if ($cur) {
                // check id is in the location map otherwise do a indexOf search
                if (!map[(id = getElementId($cur))]) {
                  // to prevent double checking
                  // mark id as found
                  map[id] = true;
                  // custom indexOf using comparitor checking oldkids[i].node === $cur
                  if ((idx = indexOfCustomNode($oldkids, $cur, j)) === -1) {
                    if (config.kids) {
                      mutations.push(
                        MutationRecord({
                          type: 'childList',
                          target: node,
                          addedNodes: [$cur], // $cur is a new node
                          nextSibling: $cur.nextSibling,
                          previousSibling: $cur.previousSibling
                        })
                      );
                      numAddedNodes++;
                    }
                  } else {
                    conflicts.push({
                      // add conflict
                      i: i,
                      j: idx
                    });
                  }
                }
                i++;
              }

              if (
                $old &&
                // special case: the changes may have been resolved: i and j appear congurent so we can continue using the expected case
                $old !== $kids[i]
              ) {
                if (!map[(id = getElementId($old))]) {
                  map[id] = true;
                  if ((idx = indexOf($kids, $old, i)) === -1) {
                    if (config.kids) {
                      mutations.push(
                        MutationRecord({
                          type: 'childList',
                          target: old.node,
                          removedNodes: [$old],
                          nextSibling: $oldkids[j + 1], // praise no indexoutofbounds exception
                          previousSibling: $oldkids[j - 1]
                        })
                      );
                      numAddedNodes--;
                    }
                  } else {
                    conflicts.push({
                      i: idx,
                      j: j
                    });
                  }
                }
                j++;
              }
            } // end uncommon case
          } // end loop

          // resolve any remaining conflicts
          if (conflicts) resolveConflicts(conflicts, node, $kids, $oldkids, numAddedNodes);
        }
        findMutations($target, $oldstate);
        return dirty;
      }

      /**
       * Utility
       * Cones a element into a custom data structure designed for comparision. https://gist.github.com/megawac/8201012
       *
       * @param {Node} $target
       * @param {!Object} config : A custom mutation config
       * @return {!Object} : Cloned data structure
       */
      function clone($target, config) {
        var recurse = true; // set true so childList we'll always check the first level
        return (function copy($target) {
          var elestruct = {
            /** @type {Node} */
            node: $target
          };

          // Store current character data of target text or comment node if the config requests
          // those properties to be observed.
          if (config.charData && ($target.nodeType === 3 || $target.nodeType === 8)) {
            elestruct.charData = $target.nodeValue;
          }
          // its either a element, comment, doc frag or document node
          else {
            // Add attr only if subtree is specified or top level and avoid if
            // attributes is a document object (#13).
            if (config.attr && recurse && $target.nodeType === 1) {
              /**
               * clone live attribute list to an object structure {name: val}
               * @type {Object.<string, string>}
               */
              elestruct.attr = reduce(
                $target.attributes,
                function (memo, attr) {
                  if (!config.afilter || config.afilter[attr.name]) {
                    memo[attr.name] = getAttributeValue($target, attr);
                  }
                  return memo;
                },
                {}
              );
            }

            // whether we should iterate the children of $target node
            if (recurse && (config.kids || config.charData || (config.attr && config.descendents))) {
              /** @type {Array.<!Object>} : Array of custom clone */
              elestruct.kids = map($target.childNodes, copy);
            }

            recurse = config.descendents;
          }
          return elestruct;
        })($target);
      }

      /**
       * indexOf an element in a collection of custom nodes
       *
       * @param {NodeList} set
       * @param {!Object} $node : A custom cloned node
       * @param {number} idx : index to start the loop
       * @return {number}
       */
      function indexOfCustomNode(set, $node, idx) {
        return indexOf(set, $node, idx, JSCompiler_renameProperty('node'));
      }

      // using a non id (eg outerHTML or nodeValue) is extremely naive and will run into issues with nodes that may appear the same like <li></li>
      var counter = 1; // don't use 0 as id (falsy)
      /** @const */
      var expando = 'mo_id';

      /**
       * Attempt to uniquely id an element for hashing. We could optimize this for legacy browsers but it hopefully wont be called enough to be a concern
       *
       * @param {Node} $ele
       * @return {(string|number)}
       */
      function getElementId($ele) {
        try {
          return $ele.id || ($ele[expando] = $ele[expando] || counter++);
        } catch (o_O) {
          // ie <8 will throw if you set an unknown property on a text node
          try {
            return $ele.nodeValue; // naive
          } catch (shitie) {
            // when text node is removed: https://gist.github.com/megawac/8355978 :(
            return counter++;
          }
        }
      }

      /**
       * **map** Apply a mapping function to each item of a set
       * @param {Array|NodeList} set
       * @param {Function} iterator
       */
      function map(set, iterator) {
        var results = [];
        for (var index = 0; index < set.length; index++) {
          results[index] = iterator(set[index], index, set);
        }
        return results;
      }

      /**
       * **Reduce** builds up a single result from a list of values
       * @param {Array|NodeList|NamedNodeMap} set
       * @param {Function} iterator
       * @param {*} [memo] Initial value of the memo.
       */
      function reduce(set, iterator, memo) {
        for (var index = 0; index < set.length; index++) {
          memo = iterator(memo, set[index], index, set);
        }
        return memo;
      }

      /**
       * **indexOf** find index of item in collection.
       * @param {Array|NodeList} set
       * @param {Object} item
       * @param {number} idx
       * @param {string} [prop] Property on set item to compare to item
       */
      function indexOf(set, item, idx, prop) {
        for (; /*idx = ~~idx*/ idx < set.length; idx++) {
          // start idx is always given as this is internal
          if ((prop ? set[idx][prop] : set[idx]) === item) return idx;
        }
        return -1;
      }

      /**
       * @param {Object} obj
       * @param {(string|number)} prop
       * @return {boolean}
       */
      function has(obj, prop) {
        return obj[prop] !== undefined$1; // will be nicely inlined by gcc
      }

      // GCC hack see https://stackoverflow.com/a/23202438/1517919
      function JSCompiler_renameProperty(a) {
        return a;
      }

      return MutationObserver;
    })(void 0);
  }

  // Taken from https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Function/bind
  if (!Function.prototype.bind) {
    Function.prototype.bind = function (oThis) {
      if (typeof this !== 'function') {
        // closest thing possible to the ECMAScript 5
        // internal IsCallable function
        throw new TypeError('Function.prototype.bind - what is trying to be bound is not callable');
      }

      var aArgs = Array.prototype.slice.call(arguments, 1),
        fToBind = this,
        fNOP = function () {},
        fBound = function () {
          return fToBind.apply(this instanceof fNOP && oThis ? this : oThis, aArgs.concat(Array.prototype.slice.call(arguments)));
        };

      fNOP.prototype = this.prototype;
      fBound.prototype = new fNOP();

      return fBound;
    };
  }

  function getUA() {
    var Sys = {};
    var ua = navigator.userAgent.toLowerCase();
    var s;
    if ((s = ua.match(/opera.([\d.]+)/))) {
      Sys.opera = Number(s[1].split('.')[0]);
    } else if ((s = ua.match(/msie ([\d.]+)/))) {
      Sys.ie = Number(s[1].split('.')[0]);
    } else if ((s = ua.match(/edge.([\d.]+)/))) {
      Sys.edge = Number(s[1].split('.')[0]);
    } else if ((s = ua.match(/firefox\/([\d.]+)/))) {
      Sys.firefox = Number(s[1].split('.')[0]);
    } else if ((s = ua.match(/chrome\/([\d.]+)/))) {
      Sys.chrome = Number(s[1].split('.')[0]);
    } else if ((s = ua.match(/version\/([\d.]+).*safari/))) {
      Sys.safari = Number(s[1].match(/^\d*.\d*/));
    } else if ((s = ua.match(/trident\/([\d.]+)/))) {
      Sys.ie = 11;
    }
    return Sys;
  }

  function validateProject() {
    if (!window.name) {
      return false;
    }
    try {
      var frameConfig = JSON.parse(decodeURIComponent(window.name || ''));

      var is_vabtesting = frameConfig.is_vabtesting;
      var source_url = frameConfig.source_url;
      var link_match_type = frameConfig.link_match_type;

      if (is_vabtesting === true && _.checkUrlIsMatch(source_url, link_match_type)) {
        return true;
      } else {
        _.log('A/B Testing SDK 页面地址，与当前实验 URL 不匹配，请使用正确的 URL！');
        return false;
      }
    } catch (e) {
      return false;
    }
  }

  /**
   * Dom 加载完毕
   *
   * @export
   * @param {Window} win 监听对象
   * @param {Function} fn 加载完毕后回调函数
   */
  function bindReady(win, fn) {
    win = win || window;
    var done = false,
      top = true,
      doc = win.document,
      root = doc.documentElement,
      modern = doc.addEventListener,
      add = modern ? 'addEventListener' : 'attachEvent',
      rem = modern ? 'removeEventListener' : 'detachEvent',
      pre = modern ? '' : 'on',
      init = function (e) {
        if (e.type == 'readystatechange' && doc.readyState != 'complete') return;
        (e.type == 'load' ? win : doc)[rem](pre + e.type, init, false);
        if (!done && (done = true)) fn.call(win, e.type || e);
      },
      poll = function () {
        try {
          root.doScroll('left');
        } catch (e) {
          setTimeout(poll, 50);
          return;
        }
        init('poll');
      };

    if (doc.readyState == 'complete') fn.call(win, 'lazy');
    else {
      if (!modern && root.doScroll) {
        try {
          top = !win.frameElement;
        } catch (e) {}
        if (top) poll();
      }
      doc[add](pre + 'DOMContentLoaded', init, false);
      doc[add](pre + 'readystatechange', init, false);
      win[add](pre + 'load', init, false);
    }
  }
  /**
   * 获取元素位置
   *
   * @export
   * @param { Element } el
   * @return { Number }
   */
  function getDomIndex(el) {
    if (!el.parentNode) return -1;
    var i = 0;
    var nodeName = el.tagName;
    var list = el.parentNode.children;
    for (var n = 0; n < list.length; n++) {
      if (list[n].tagName === nodeName) {
        if (el === list[n]) {
          return i;
        } else {
          i++;
        }
      }
    }
    return -1;
  }
  /**
   * 获取当前元素的 selector
   *
   * @export
   * @param { Element } el
   * @return { String }
   */
  function selector(el) {
    var i = el.parentNode && 9 == el.parentNode.nodeType ? -1 : getDomIndex(el);
    if (el.getAttribute && el.getAttribute('id') && /^[A-Za-z][-A-Za-z0-9_:.]*$/.test(el.getAttribute('id'))) {
      return '#' + el.getAttribute('id');
    } else {
      return el.tagName.toLowerCase() + (~i ? ':nth-of-type(' + (i + 1) + ')' : '');
    }
  }
  /**
   * 获取元素的完整 Selector
   *
   * @export
   * @param { Element } el
   * @param { Array<Element> } arr
   * @return { String }
   */
  function getDomSelector(el, arr) {
    if (!el || !el.parentNode || !el.parentNode.children) {
      return false;
    }
    arr = arr && arr.join ? arr : [];
    var name = el.nodeName.toLowerCase();
    if (!el || name === 'body' || 1 != el.nodeType) {
      arr.unshift('body');
      return arr.join(' > ');
    }
    arr.unshift(selector(el));
    if (el.getAttribute && el.getAttribute('id') && /^[A-Za-z][-A-Za-z0-9_:.]*$/.test(el.getAttribute('id'))) return arr.join(' > ');
    return getDomSelector(el.parentNode, arr);
  }
  /**
   * 获取计算后的元素样式
   *
   * @export
   * @param { Element } el 元素
   * @param { String } type 元素样式名称
   * @return { String | Number } 元素样式的值
   */
  function getStyle(el, type) {
    if (el.currentStyle) {
      return el.currentStyle[type];
    } else {
      return el.ownerDocument.defaultView.getComputedStyle(el, null).getPropertyValue(type);
    }
  }

  /**
   * 获取范围内的元素样式
   *
   * @export
   * @param { Element } ele 元素
   * @return { Object } 元素属性信息
   */
  function getElementStyles(ele, styles) {
    var originStyle = {};
    _.each(styles || [], function (val) {
      var kebab_val = val.replace(/[A-Z]+/g, function (s) {
        return '-' + s.toLowerCase();
      });
      originStyle[val] = getStyle(ele, kebab_val);
    });
    return originStyle;
  }
  /**
   * 获取元素内容
   *
   * @export
   * @param { Element } ele
   * @return { String } 内容
   */
  function getElementText(ele) {
    var children = ele.childNodes;
    var text = '';
    var is_change = false;
    _.each(children, function (el) {
      if (is_change === false && el.nodeType === 3 && (_.isString(el.textContent) || _.isString(el.innerText) || _.isString(el.nodeValue))) {
        text = _.trim(el.textContent || el.innerText || el.nodeValue || '');
        is_change = true;
      }
    });
    return text;
  }

  /**
   * 根据 selector 获取 Dom
   *
   * @export
   * @param { String } selector
   * @return { Element | null}
   */
  function getDomBySelector(selector) {
    if (!_.isString(selector)) {
      return null;
    }
    var arr = selector.split('>');
    var el = null;

    function getDom(selector, parent) {
      selector = _.trim(selector);
      var node;
      if (selector === 'body') {
        return document.getElementsByTagName('body')[0];
      }
      if (selector.indexOf('#') === 0) {
        //如果是id选择器 #login
        selector = selector.slice(1);
        node = document.getElementById(selector);
      } else if (selector.indexOf(':nth-of-type') > -1) {
        //div:nth-of-type(1)
        var arr = selector.split(':nth-of-type');
        if (!(arr[0] && arr[1])) {
          //格式不正确，返回空
          return null;
        }
        var tagname = arr[0];
        var indexArr = arr[1].match(/\(([0-9]+)\)/);
        if (!(indexArr && indexArr[1])) {
          //没有匹配到正确的标签序号，返回空
          return null;
        }
        var num = Number(indexArr[1]); //标签序号
        if (!(_.isElement(parent) && parent.children && parent.children.length > 0)) {
          return null;
        }
        var child = parent.children;

        for (var i = 0; i < child.length; i++) {
          if (_.isElement(child[i])) {
            var name = child[i].tagName.toLowerCase();
            if (name === tagname) {
              num--;
              if (num === 0) {
                node = child[i];
                break;
              }
            }
          }
        }
        if (num > 0) {
          //子元素列表中未找到
          return null;
        }
      }
      if (!node) {
        return null;
      }
      return node;
    }

    function get(parent) {
      var tagSelector = arr.shift();
      var element;
      if (!tagSelector) {
        return parent;
      }
      try {
        element = getDom(tagSelector, parent);
      } catch (error) {
        element = null;
      }
      if (!(element && _.isElement(element))) {
        return null;
      } else {
        return get(element);
      }
    }
    el = get();
    if (!(el && _.isElement(el))) {
      return null;
    } else {
      return el;
    }
  }
  /**
   *
   *
   * @export
   * @param {String} color
   * @return {*}
   */
  function rgbaToRgb(color) {
    var rgbaAttr = color.match(/[\d.]+/g);
    if (rgbaAttr.length >= 3) {
      var r, g, b;
      r = rgbaAttr[0];
      g = rgbaAttr[1];
      b = rgbaAttr[2];
      return 'rgb(' + r + ',' + g + ',' + b + ')';
    }
    return '';
  }

  /**
   * 修改元素样式
   *
   * @export
   * @param { Element } ele 页面元素
   * @param { Object } styles 样式列表及值
   */
  function changeStyle(ele, styles) {
    var Sys = getUA();
    var is_low_ie = Sys.ie && Sys.ie < 9;
    var ele_styles = '';
    _.each(styles, function (val, key) {
      if (is_low_ie && val.indexOf('rgba') > -1) {
        val = rgbaToRgb(val);
      }
      var kebab_val = key.replace(/[A-Z]+/g, function (s) {
        return '-' + s.toLowerCase();
      });
      ele_styles += ';' + kebab_val + ': ' + val;
    });
    if (ele_styles === '') return;

    function endsWith(str, suffix) {
      var l = str.length - suffix.length;
      return l >= 0 && str.indexOf(suffix, l) == l;
    }
    var sty = ele.style,
      cssText = sty.cssText || '';
    if (cssText && !endsWith(cssText, ';')) {
      cssText += ';';
    }
    sty.cssText = cssText + ele_styles;
  }
  /**
   * 修改元素内容，多个 node 节点，仅修改第一个节点，其他节点清空
   *
   * @export
   * @param { Element } ele 元素
   * @param { String } content 元素内容
   */
  function changeText(ele, content) {
    var children = ele.childNodes;
    var is_change = false;
    _.each(children, function (el) {
      if (el.nodeType === 3) {
        if (!is_change) {
          if (el.textContent) {
            el.textContent = content;
          } else if (el.innerText) {
            el.innerText = content;
          } else {
            el.nodeValue = content;
          }
          is_change = true;
        }
      }
    });
  }
  /**
   * 原有属性与修改数据中的原有属性进行对比，不一致则修改
   *
   * @export
   * @param { JSON } attr 修改数据中的原有属性
   * @param { JSON } element_attr 原有属性
   * @return { Boolean }
   */
  function propsFilter(attr, element_attr) {
    var props = attr.originProps;
    var element_props = element_attr.originProps;
    var props_attributes = props.attributes;
    var element_props_attributes = element_props.attributes;
    var status = true;

    if (props.text && props.text !== element_props.text) {
      status = false;
    }
    if (props_attributes) {
      _.each(['src', 'href', 'target'], function (val) {
        if (props_attributes[val] && props_attributes[val] !== element_props_attributes[val]) {
          status = false;
        }
      });
    }

    return status;
  }
  /**
   * 修改元素 Attribute
   *
   * @export
   * @param { Element } ele 元素
   * @param { String } key Attribute 名称
   * @param { String } val Attribute 值
   */
  function changeAttribute(ele, key, val) {
    ele.setAttribute(key, val);
  }
  /**
   * 根据获取范围，获取元素原有属性
   *
   * @export
   * @param { Element } ele 元素
   * @param { JSON } attr 属性范围
   * @return { JSON }
   */
  function getElementPropsByAttr(ele, attr) {
    var props = attr.originProps;
    var element_props = { attributes: {} };
    var props_attributes = props.attributes;
    _.each(props_attributes.style || {}, function (val, key) {
      element_props.attributes.style[key] = getStyle(ele, key);
    });
    if (Object.hasOwnProperty.call(props, 'text')) {
      element_props.attributes.text = getElementText(ele);
    }
    _.each(['src', 'href'], function (val) {
      if (Object.hasOwnProperty.call(props_attributes, val)) {
        element_props.attributes[val] = ele[val];
      }
    });
    if (Object.hasOwnProperty.call(props_attributes, 'target')) {
      element_props.attributes.target = ele.getAttribute('target');
    }
    return element_props;
  }
  /**
   * 对比并修改元素样式及内容
   *
   * @export
   * @param {Element} ele
   * @param { JSON } attr
   * @param {JSON} element_attr
   */
  function changeAttributes(ele, attr, element_attr) {
    if (!element_attr) {
      element_attr = getElementPropsByAttr(ele, attr);
    }
    if (propsFilter(attr, element_attr)) {
      var props = attr.props;
      if (_.isObject(props)) {
        setAttributes(ele, props);
      }
    }
  }
  /**
   * 根据设置内容，设置元素属性
   *
   * @export
   * @param { Element } ele 元素
   * @param { JSON } props 元素属性设置范围
   */
  function setAttributes(ele, props) {
    if (Object.hasOwnProperty.call(props, 'attributes')) {
      var attributes = props.attributes;
      if (Object.hasOwnProperty.call(attributes, 'style')) {
        changeStyle(ele, attributes.style);
      }
      _.each(['href', 'target', 'src'], function (val) {
        if (Object.hasOwnProperty.call(attributes, val)) {
          changeAttribute(ele, val, attributes[val]);
        }
      });
    }
    if (Object.hasOwnProperty.call(props, 'text')) {
      changeText(ele, props.text);
    }
  }
  /**
   * 加载 script 标签 或 link 标签
   *
   * @export
   * @param { JSON } para 加载内容
   */
  function loadScript(para) {
    para = _.extend(
      {
        success: function () {},
        error: function () {},
        appendCall: function (g) {
          document.getElementsByTagName('head')[0].appendChild(g);
        }
      },
      para
    );

    var g = null;
    if (para.type === 'css') {
      g = document.createElement('link');
      g.rel = 'stylesheet';
      g.href = para.url;
    }
    if (para.type === 'js') {
      g = document.createElement('script');
      g.async = 'async';
      g.setAttribute('charset', 'UTF-8');
      g.src = para.url;
      g.type = 'text/javascript';
    }
    if (para.id) {
      g.id = para.id;
    }
    g.onload = g.onreadystatechange = function () {
      if (!this.readyState || this.readyState === 'loaded' || this.readyState === 'complete') {
        para.success();
        g.onload = g.onreadystatechange = null;
      }
    };
    g.onerror = function () {
      para.error();
      g.onerror = null;
    };
    // if iframe
    para.appendCall(g);
  }
  /**
   * 加载 vtesting sdk
   *
   * @export
   */
  function loadVtesting(url, para) {
    loadScript(
      _.extend(
        {
          success: function () {},
          error: function () {},
          type: 'js',
          url: url
        },
        para
      )
    );
  }

  function addMutationObserver(callback) {
    // 观察器的配置（需要观察什么变动）
    var config = { childList: true, subtree: true };

    var MutationObserver = window.MutationObserver;
    // 创建一个观察器实例
    var observer = new MutationObserver(callback);

    // 以上述配置开始观察目标节点
    observer.observe(document.body, config);
  }

  /**
   * 获取元素原有属性及原有 style 内容
   *
   * @export
   * @param { Element } ele 元素
   * @return { JSON }
   */
  function getInfoByElement(ele, styles) {
    var tagName = ele.tagName.toLowerCase();
    var ele_styles = getElementStyles(ele, styles);
    var eleInfo = {
      selector: getDomSelector(ele),
      originProps: {
        attributes: {
          style: ele_styles
        },
        text: getElementText(ele)
      },
      type: 'text',
      tagName: tagName
    };
    if (tagName === 'a') {
      eleInfo.originProps.attributes.href = ele.href;
      if (ele.getAttribute('target')) {
        eleInfo.originProps.attributes.target = ele.getAttribute('target');
      }
      eleInfo.type = 'link';
    }
    if (tagName === 'img') {
      eleInfo.originProps.attributes.src = ele.src;
      eleInfo.type = 'img';
    }
    var ele_style = ele.getAttribute('style');
    return {
      originStyle: ele_style,
      eleInfo: eleInfo,
      ele: ele
    };
  }
  /**
   * 还原单一元素原始样式及内容
   *
   * @param {*} ele
   * @param {*} style
   */
  function restoreElement(originData) {
    var ele_info = originData.eleInfo;
    var ele = originData.ele;
    if (ele) {
      var style = originData.originStyle;
      setAttributes(ele, ele_info.originProps);
      if (style) {
        ele.style.cssText = style;
      } else {
        ele.style.cssText = '';
      }
    }
  }

  /**
   * abtesting 可视化实验
   *
   */
  function Vabtesting(SensorsABTest) {
    this.SensorsABTest = SensorsABTest;
    this.enable_visualize = false; // 开启
    this.vabtest_url = location.protocol + '//static.sensorsdata.cn/sdk/plugin/vabtest/' + this.SensorsABTest.lib_version + '/vabtest.min.js';
    this.timeout = 500;
    this.is_editor = false;
    this.use_mask = true;
    this.originData = [];
    this.visualExp = null;
    this.isFirst = true;
    this.start_time = null;
    this.is_have_observer = false;
    this.imgLoadingList = [];
    this.isTrigger = false;
  }
  /**
   * 页面浏览插件初始化方法
   * @param {String} option
   */
  Vabtesting.prototype.init = function (para) {
    var _this = this;
    this.initPara(para);
    if (this.enable_visualize === false) return;

    // 判断是否为 iframe 内嵌模式
    if (window.self !== window.top && validateProject()) {
      this.is_editor = true;
      // 防止重复加载
      if (!document.getElementById('sa-abtesting-vabtest-script')) {
        loadVtesting(this.vabtest_url, {
          id: 'sa-abtesting-vabtest-script',
          success: function () {
            setTimeout(function () {
              _this.SensorsABTest.sd.use('VABTest', {
                SensorsABTest: _this.SensorsABTest
              });
            }, 0);
          }
        });
      }
    } else {
      this.start_time = new Date().getTime();
      if (this.use_mask) {
        this.initMask();
      }
      this.addSinglePageListener();
    }
  };

  /**
   * 初始化 vabtesting 配置
   * @param {String} option
   */
  Vabtesting.prototype.initPara = function (para) {
    if (_.isFunction(para)) {
      try {
        para = para();
      } catch (error) {
        this.SensorsABTest.log('ABTesting SDK  !');
        this.para = false;
        return;
      }
    }
    if (!_.isObject(para)) {
      this.para = false;
      return;
    }
    if (Object.prototype.hasOwnProperty.call(para, 'enable_visualize') && _.isBoolean(para.enable_visualize)) {
      this.enable_visualize = para.enable_visualize;
    } else {
      this.SensorsABTest.log('ABTesting SDK 初始化参数 enable_visualize 格式不正确。 类型应为 Boolean');
    }
    this.use_mask = _.isBoolean(para.use_mask) ? para.use_mask : this.use_mask;
    this.timeout = _.isNumber(para.timeout) && para.timeout >= 0 ? para.timeout : this.timeout;

    var vabtest_url = _.isString(para.vabtest_url) ? para.vabtest_url : '';
    if (!vabtest_url) return;

    if (location.protocol === 'https:') {
      if (vabtest_url.slice(0, 6) === 'https:') {
        this.vabtest_url = vabtest_url;
      } else {
        this.SensorsABTest.log('ABTesting SDK 初始化参数 vabtest_url 协议头应设置为 https');
      }
    } else {
      if (vabtest_url.slice(0, 5) === 'http:' || vabtest_url.slice(0, 6) === 'https:') {
        this.vabtest_url = vabtest_url;
      } else {
        this.SensorsABTest.log('ABTesting SDK 初始化参数 vabtest_url 应设置为有效 URL');
      }
    }
  };

  /**
   * 校验及处理 AB 可视化实验结果
   * @param { JSON } result
   * @returns
   */
  Vabtesting.prototype.resolve = function () {
    var result = this.SensorsABTest.results;
    if (this.enable_visualize === false || this.is_editor === true || this.isFirst === false) return;

    if (new Date().getTime() - this.start_time > this.timeout) {
      this.SensorsABTest.log('请求实验结果超时，A/B 可视化实验失败！');
      return false;
    }
    var _this = this;
    this.isFirst = false;
    var is_trigger = false;
    _.each(result, function (exp) {
      if (!_.isObject(exp) || exp.experiment_type !== 'VISUAL') {
        return false;
      }
      if (is_trigger) {
        return false;
      }
      var exp_value = exp.experiment_value;
      if (!_.isArray(exp_value)) {
        try {
          exp_value = JSON.parse(exp.experiment_value);
          exp.experiment_value = exp_value;
        } catch (e) {
          return false;
        }
      }
      if (_this.isTriggerVisualExp(exp)) {
        is_trigger = true;
        _this.prepareResource(exp, _this.render.bind(_this));
        _this.visualExp = exp;
      }
    });
    if (!is_trigger && this.use_mask) {
      this.destroyMask();
    }
    bindReady(window, function () {
      addMutationObserver(_this.observerCallback.bind(_this));
    });
  };
  /**
   * 初始化白色全屏遮罩层
   */
  Vabtesting.prototype.initMask = function () {
    this.mask_instance = new Mask('vabtest' + '_' + this.SensorsABTest.sd.para.sdk_id);
    this.mask_instance.show(this.timeout);
  };

  /**
   * 移除全屏遮罩层
   */
  Vabtesting.prototype.destroyMask = function () {
    this.mask_instance && this.mask_instance.remove();
  };

  /**
   * 图片预加载，加载后渲染页面
   */
  Vabtesting.prototype.prepareResource = function (exp, onComplete) {
    var _this = this;
    var experiments = exp.experiment_value;
    if (_.isArray(experiments)) {
      _.each(experiments, function (val) {
        var tagName = val.tagName;
        var props = val.props;
        if (tagName !== 'img' || !props) return;
        if (!props.attributes) return;
        if (!_.isString(props.attributes.src)) return;
        var path = props.attributes.src;
        _this.imgLoadingList.push(path);

        function hook() {
          var index = _this.imgLoadingList.indexOf(path);
          if (index > -1) {
            _this.imgLoadingList.splice(index, 1);
          }
          if (_this.imgLoadingList.length === 0) {
            onComplete(exp);
          }
        }
        var img = document.createElement('img');
        img.width = 1;
        img.height = 1;
        img.onload = hook;
        img.onerror = hook;
        img.onabort = hook;
        img.src = props.attributes.src;
      });
      if (this.imgLoadingList.length === 0) {
        onComplete(exp);
      }
    }
  };
  /**
   * 渲染修改内容render
   */
  Vabtesting.prototype.render = function (exp) {
    var _this = this;
    bindReady(window, function () {
      // 页面加载完毕且图片资源加载完毕前，遮罩超时，则不触发实验
      if (_this.use_mask && _this.mask_instance && _this.mask_instance.is_timeout === true) {
        _this.SensorsABTest.log('渲染实验结果超时，A/B 可视化实验失败！');
        return;
      }
      if (!_this.isTrigger) {
        // 页面加载完毕，且遮罩未超时，发送触发实验结果
        _this.SensorsABTest.triggerHandle(exp);
        _this.isTrigger = true;
      }
      var experiments = exp.experiment_value;
      if (_.isArray(experiments)) {
        _.each(experiments, function (val) {
          var selector = val.selector;
          var ele = getDomBySelector(selector);
          if (ele) {
            try {
              var ele_info = getInfoByElement(ele, []);
              var origin_props = ele_info.eleInfo;
              changeAttributes(ele, val, origin_props);
              _this.originData.push(ele_info);
            } catch (e) {}
          }
        });
      }
      if (_this.use_mask) {
        _this.destroyMask();
      }
    });
  };

  Vabtesting.prototype.restoreElements = function () {
    _.each(this.originData, function (val) {
      restoreElement(val);
    });
    this.visualExp = null;
    this.isFirst = true;
    this.start_time = new Date().getTime();
    this.originData = [];
    this.isTrigger = false;
  };

  /**
   * 创建单页面监听
   */
  Vabtesting.prototype.addSinglePageListener = function () {
    var _this = this;
    _.addSinglePageEvent(function (last_url) {
      if (last_url === location.href) {
        return false;
      }

      if (_this.mask_instance) {
        _this.destroyMask();
      }
      _this.start_time = new Date().getTime();
      if (_this.use_mask) {
        _this.initMask();
      }
      _this.restoreElements();

      _this.resolve();
    });
  };

  /**
   * 页面 dom 变化后回调函数
   */
  Vabtesting.prototype.observerCallback = function () {
    if (this.visualExp) {
      _.each(this.originData, function (val) {
        restoreElement(val);
      });
      this.originData = [];
      this.render(this.visualExp);
    }
  };

  Vabtesting.prototype.stopTrigger = function () {
    this.isFirst = false;
    this.SensorsABTest.log('请求实验结果失败，A/B 可视化实验失败！');
    if (this.use_mask) {
      this.destroyMask();
    }
  };
  /**
   * 页面 dom 变化后回调函数
   * @param { JSON } exp 实验信息
   * @returns { Boolean } 匹配结果
   */

  Vabtesting.prototype.isTriggerVisualExp = function (exp) {
    if (!(_.isString(exp.control_link) && _.isString(exp.link_match_type))) {
      this.SensorsABTest.log('可视化试验数据异常', exp.abtest_experiment_id);
      return false;
    }
    if (_.checkUrlIsMatch(exp.control_link, exp.link_match_type)) {
      return true;
    } else {
      this.SensorsABTest.log('可视化试验匹配失败', exp.abtest_experiment_id);
      return false;
    }
  };

  function SensorsABTest() {
    this.lib_version = '1.24.10';
    this.plugin_version = '1.24.10';
    this.plugin_name = 'SensorsABTest';
    this.para = {};
    this.default_para = {
      url: '',
      path: '',
      project_key: '',
      timeout_milliseconds: 3000, // 试验拉取超时时间
      update_interval: 10 * 60 * 1000, // 试验刷新时间
      collect_bridge_status: true,
      encrypt_cookie: false // 默认不使用加密
    };
    this.state = {
      platform: '',
      storage: {
        name: 'sawebjssdkabtest'
      }
    };
    this.bridgeState = '';
    this.codeExpData = {}; // 解析后的编程试验数据
    this.results = []; // 原始数据
    this.out_list = []; // 出组数据
    this.triggerList = {}; // 已命中试验列表
    this.outTriggerList = {}; // 已出组试验列表
    this.default_track_config = {
      // 默认埋点开关配置
      trigger_switch: true,
      property_set_switch: false,
      trigger_content_ext: ['abtest_experiment_version', 'abtest_experiment_result_id']
    };
    this.track_config = {}; // 试验埋点开关配置

    this.updateTime = null;
    this.sd = null;
    this.link = new Link(this);
    this.vabtest = new Vabtesting(this);
    this.normalStore = new NormalStore(this);
    this.bridgeStore = new BridgeStore(this);
    this.verifyStore = new VerifyStore(this);
  }

  /**
   * 代码试验
   */

  //未初始化或者初始化失败分流API不支持调用
  SensorsABTest.prototype.asyncFetchABTest = function () {
    _.error('asyncFetchABTest调用失败,A/B Testing未初始化');
  };
  SensorsABTest.prototype.fastFetchABTest = function () {
    _.error('fastFetchABTest调用失败,A/B Testing未初始化');
  };
  SensorsABTest.prototype.fetchCacheABTest = function () {
    _.error('fetchCacheABTest调用失败,A/B Testing未初始化');
  };

  /**
   * 查找localdata中是否有该试验key的数据，没有返回null
   * @param {String} param_name 试验参数名称
   * return 试验结果对象
   */
  SensorsABTest.prototype.searchLocalExp = function (param_name) {
    if (this.codeExpData[param_name]) {
      return this.codeExpData[param_name];
    }
    return null;
  };

  /**
   * 1、获取本地试验结果。localdata中有试验数据，数据类型与para.value_type一致才会返回试验值；否则返回默认值
   * 2、成功命中试验触发 $ABTestTrigger 事件
   * @param {Object} para 分流API参数
   * @param {*} obj 如果已获取试验对象传入
   * return 最终的试验value（试验值或默认值）
   */
  SensorsABTest.prototype.getExpResult = function (para, obj) {
    var result = para.default_value;
    var expObj = obj ? obj : this.searchLocalExp(para.param_name);

    if (_.isObject(expObj)) {
      if (_.isObject(expObj.js_config)) {
        if (expObj.js_config.type === para.value_type) {
          result = expObj.js_config.value;
          this.triggerHandle(expObj, para);
        } else {
          this.log('试验结果类型与代码期望类型不一致，param_name：' + para.param_name + '，当前返回类型为：' + expObj.js_config.type + '，代码期望类型为：' + para.value_type);
        }
      }
    } else {
      this.log('localdata未查询到试验数据，试验参数名称：' + para.param_name);
    }
    return result;
  };

  /**
   * 解析代码试验数据到localdata中
   * @param {*} result
   */
  SensorsABTest.prototype.resolveVariables = function () {
    this.codeExpData = {};
    var _this = this;
    var data = this.results;
    _.each(data, function (exp) {
      if (_.isObject(exp) && exp.variables && _.isArray(exp.variables)) {
        if (exp.experiment_type && exp.experiment_type !== 'CODE') {
          return false;
        }
        _.each(exp.variables, function (val) {
          //相同的key只用前面的
          if (_.isObject(val) && !_this.codeExpData[val.name]) {
            _this.codeExpData[val.name] = _.extend({}, exp);
            _this.codeExpData[val.name].js_config = _this.getRelativeValue(val.value, val.type);
          }
        });
      }
    });
  };

  /**
   * 将服务端返回的试验variables根据js的type进行转换
   * @param {String} val 服务端返回的 value
   * @param {String} type 服务端返回的 type
   * return {value:123,type:'Number'}
   */
  SensorsABTest.prototype.getRelativeValue = function (val, type) {
    var _this = this;
    var data = {};
    var change_list = {
      INTEGER: function (value) {
        var val = parseFloat(value);
        if (!isNaN(val)) {
          data.value = val;
          data.type = 'Number';
        } else {
          _this.log('原始数据 INTEGER 类型解析异常', value);
        }
      },
      STRING: function (value) {
        if (_.isString(value)) {
          data.value = value;
          data.type = 'String';
        } else {
          _this.log('原始数据 STRING 类型解析异常', value);
        }
      },
      JSON: function (value) {
        var val = JSON.parse(value);
        if (_.isObject(val)) {
          data.value = val;
          data.type = 'Object';
        } else {
          _this.log('原始数据 JSON 类型解析异常', value);
        }
      },
      BOOLEAN: function (value) {
        if (value === 'true') {
          data.value = true;
          data.type = 'Boolean';
        } else if (value === 'false') {
          data.value = false;
          data.type = 'Boolean';
        } else {
          _this.log('原始数据 BOOLEAN 类型解析异常', value);
        }
      }
    };
    try {
      if (change_list[type]) {
        change_list[type](val);
      } else {
        _this.log('试验数据类型解析失败', type, val);
      }
    } catch (error) {
      _this.log(error, val, type);
    }
    return data;
  };

  /**
   * 通用的业务方法
   */

  /**
   * 处理服务端response
   * @param {Object} data responsedata
   */
  SensorsABTest.prototype.dealResponseData = function (data, request_id) {
    if (_.isObject(data)) {
      if (data.status === 'SUCCESS') {
        if (_.isArray(data.results)) {
          // 更新缓存记录
          this.fetchData.updateExpsCache(data);
          // 保存原始数据到内存
          this.updateLocalData(request_id);
        }
      } else {
        if (data.status === 'FAILED') {
          this.log('获取试验失败：error_type：' + data.error_type + ',error：' + data.error);
        }
      }
    } else {
      this.log('试验数据解析失败，response ：', data);
    }
  };

  //更新本地数据（全量更新） 内存+storage
  SensorsABTest.prototype.updateLocalData = function (request_id) {
    //解析数据
    this.analyzeData();
    //保存数据到storage
    this.updateStorage(request_id);
    this.log('更新试验数据成功');
  };
  //更新storage
  SensorsABTest.prototype.updateStorage = function (request_id) {
    var time = new Date().getTime();
    var data = {
      results: this.results,
      updateTime: time,
      triggerList: this.triggerList,
      distinct_id: request_id || this.sd.store.getDistinctId(),
      outTriggerList: this.outTriggerList,
      out_list: this.out_list,
      track_config: this.track_config
    };
    var name = this.state.storage.name;
    var encrypt_cookie = this.para.encrypt_cookie;
    setStorageData(data, name, encrypt_cookie);
    this.updateTime = time;
  };
  /**
   * 解析数据
   * @param {*} data 原始数据 result
   */
  SensorsABTest.prototype.analyzeData = function () {
    var data = this.results;
    if (!_.isArray(data)) {
      this.log('解析——数据格式错误', data);
      return false;
    }
    this.link.resolve();

    this.vabtest.resolve();

    this.resolveVariables();
    // 更新公共属性
    this.registerProperty();
    // 检测出组情况
    this.trackOutTestTrigger();
  };

  function updateTriggerData(triggerList, expObj) {
    var triggerData = [];
    if (_.isArray(triggerList)) {
      _.each(triggerList, function (trigger, index) {
        if (_.isString(trigger)) {
          triggerList.splice(index, 1, {
            experiment_id: trigger
          });
        }
      });
    }
    //已命中试验更新到triggerList
    var experiment_id = expObj.abtest_experiment_id;
    if (_.isString(experiment_id)) {
      var experiment = {
        experiment_id: experiment_id,
        group_id: expObj.abtest_experiment_group_id,
        result_id: expObj.abtest_experiment_result_id
      };
      if (triggerList && _.isArray(triggerList)) {
        // 更新命中试验列表
        var is_in_trigger = false;
        _.each(triggerList, function (trigger, index) {
          if (trigger.experiment_id === experiment_id) {
            triggerList.splice(index, 1, experiment);
            is_in_trigger = true;
          }
        });
        if (!is_in_trigger) {
          triggerList.push(experiment);
        }
      } else {
        triggerList = [experiment];
      }
      triggerData = triggerList;
    }
    return triggerData;
  }
  SensorsABTest.prototype.trackOutTestTrigger = function () {
    var out_list = this.out_list;
    var _this = this;
    if (_.isArray(out_list)) {
      _.each(out_list, function (expObj) {
        var distinctId = _this.sd.store.getDistinctId();
        if (expObj.subject_id && expObj.subject_name) {
          // 新版本下发自定义主体内容，不再使用 distinctId 作为用户标识
          distinctId = expObj.subject_name + expObj.subject_id;
        }
        _this.sendTriggerEvent('outTriggerList', distinctId, expObj, {});
      });
    }
  };

  SensorsABTest.prototype.registerProperty = function () {
    var property_set_switch = this.track_config.property_set_switch;
    if (property_set_switch) {
      var abtest_result = [];
      var abtest_dispatch_result = [];
      _.each(this.triggerList, function (triggerData) {
        if (_.isArray(triggerData)) {
          _.each(triggerData, function (e) {
            e.result_id && abtest_dispatch_result.push(e.result_id);
          });
        }
      });
      if (_.isArray(this.results)) {
        _.each(this.results, function (v) {
          v.abtest_experiment_result_id && abtest_result.push(v.abtest_experiment_result_id);
        });
      }
      this.sd.registerPage({
        abtest_result: abtest_result,
        abtest_dispatch_result: abtest_dispatch_result
      });
    } else {
      this.sd.clearPageRegister(['abtest_result', 'abtest_dispatch_result']);
    }
  };

  //成功命中试验后的动作1、触发trigger事件2、命中记录保存在storage
  SensorsABTest.prototype.triggerHandle = function (expObj, para) {
    var distinctId = this.sd.store.getDistinctId();
    if (expObj.subject_id && expObj.subject_name) {
      // 新版本下发自定义主体内容，不再使用 distinctId 作为用户标识
      distinctId = expObj.subject_name + expObj.subject_id;
    }

    var isTrack = this.trackTestTrigger(distinctId, expObj, para);

    return isTrack;
  };

  SensorsABTest.prototype.getTriggerProps = function (expObj, para) {
    var props = {};
    var properties = _.isObject(para) && _.isObject(para.properties) ? para.properties : {};

    if (JSON.stringify(this.triggerList) !== '{}') {
      var lib = 'web_abtesting:' + this.lib_version;
      props.$lib_plugin_version = [lib];
    }
    if (this.para.collect_bridge_status) {
      props.$sdk_bridge_status = this.bridgeState;
    }

    var obj = {
      $abtest_experiment_id: expObj.abtest_experiment_id,
      $abtest_experiment_group_id: expObj.abtest_experiment_group_id
    };

    var trigger_content_ext = this.track_config.trigger_content_ext;
    _.each(trigger_content_ext, function (v) {
      if (expObj[v] !== void 0) {
        props['$' + v] = expObj[v];
      }
    });
    props = _.extend(obj, props, properties);
    return props;
  };

  SensorsABTest.prototype.sendTriggerEvent = function (cacheKey, distinctId, expObj, para) {
    var trigger_switch = this.track_config.trigger_switch;

    var isTrack = true;
    var cacheTriggerList = this[cacheKey];
    if (cacheTriggerList && _.isObject(cacheTriggerList)) {
      if (cacheTriggerList[distinctId]) {
        _.each(cacheTriggerList[distinctId], function (item) {
          if (item.experiment_id === expObj.abtest_experiment_id && item.group_id === expObj.abtest_experiment_group_id && item.result_id === expObj.abtest_experiment_result_id) {
            isTrack = false;
          }
        });
      }
    }

    this[cacheKey][distinctId] = updateTriggerData(cacheTriggerList, expObj);
    this.updateStorage();
    this.registerProperty();
    var trackProps = this.getTriggerProps(expObj, para);

    if (isTrack && trigger_switch) {
      this.sd.track('$ABTestTrigger', trackProps);
    }
    return isTrack;
  };

  /**
   * 上报试验触发事件
   * 1、白名单用户不上报
   * 2、一个用户重复命中一个试验id不上报
   * 3、只有第一个事件上报$lib_plugin_version
   * @param {object} expObj
   * @param {object} otherObj
   */
  SensorsABTest.prototype.trackTestTrigger = function (distinctId, expObj, para) {
    if (expObj.is_white_list) {
      return false;
    }
    var isTrack = this.sendTriggerEvent('triggerList', distinctId, expObj, para);

    var trackProps = this.getTriggerProps(expObj, para);
    this.sd.track('WebABTestTrigger', trackProps);
    return isTrack;
  };

  /**
   * debug模式下，把当前用户id发送给服务端添加白名单
   */
  SensorsABTest.prototype.checkSADebug = function () {
    var _this = this;
    var abtest_url = _.getQueryParam(location.href, 'sensors_abtest_url');
    var feature_code = _.getQueryParam(location.href, 'feature_code');
    var account_id = +_.getQueryParam(location.href, 'account_id');
    //从url上解析到这三个参数调试用户才能成功匹配
    if (abtest_url.length && feature_code.length && _.isNumber(account_id) && account_id !== 0) {
      var data = {
        distinct_id: this.sd.store.getDistinctId(),
        //后端用来保存distinct_id的标识
        feature_code: feature_code,
        //number，登陆到SA的账号ID
        account_id: account_id
      };
      this.sd._.ajax({
        url: abtest_url,
        type: 'POST',
        data: JSON.stringify(data),
        credentials: false,
        contentType: 'application/json',
        timeout: this.para.timeout_milliseconds,
        cors: true,
        success: function () {},
        error: function (err) {
          _this.log('distinct_id发送失败,err:', err);
        }
      });
    }
  };

  //初始化分流API
  SensorsABTest.prototype.initMethods = function (context) {
    var _this = this;
    var methods = ['asyncFetchABTest', 'fastFetchABTest', 'fetchCacheABTest'];
    _.each(methods, function (key) {
      _this[key] = context.methods[key].bind(context);
    });
  };
  function FetchData(SensorsABTest) {
    this.SensorsABTest = SensorsABTest;
  }
  FetchData.prototype = {
    timer: null,
    method: null,
    context: null,
    /**
     * 初始化拉取数据，没满足拉取间隔时间就不拉取，到十分钟了再拉取
     * @param {Function} method 拉取方法（服务器/App）
     * @param {object} context 拉取方法执行的上下文对象
     */
    init: function (method, context) {
      this.method = method;
      this.context = context;
      this.start(true);
    },
    getServerData: function (isFirst) {
      var _this = this;
      if (isFirst) {
        this.method.call(this.context, {
          suc: function (data) {
            if (isFirst) {
              if (_this.SensorsABTest.bridgeState === 'ab_bridge_ok') {
                data = data.data;
              }
              if (!(_.isObject(data) && data.status === 'SUCCESS' && _.isArray(data.results))) {
                _this.SensorsABTest.link.stopTrigger(); //首次拉取试验数据失败直接结束多链接试验
                _this.SensorsABTest.vabtest.stopTrigger(); //首次拉取试验数据失败直接结束可视化试验
              }
            }
          },
          err: function () {
            if (isFirst) {
              _this.SensorsABTest.link.stopTrigger(); //首次拉取试验数据失败直接结束多链接试验
              _this.SensorsABTest.vabtest.stopTrigger(); //首次拉取试验数据失败直接结束可视化试验
            }
          }
        });
      } else {
        this.method.call(this.context);
      }
    },
    //设置下一次拉取动作
    setNextFetch: function (time) {
      var _this = this;
      var nextTime = time || this.SensorsABTest.para.update_interval;
      this.clearFetchTimer(this.timer);
      this.timer = setTimeout(function () {
        _this.getServerData();
      }, nextTime);
    },
    /**
     * 首次或试验超时，通过拉取或获取 localStorage 更新缓存记录
     *
     */
    updateExpsCache: function (data) {
      // 分流试验列表
      this.SensorsABTest.results = data.results || data.data || [];
      var triggerList = data.triggerList;
      var outList = data.out_list;
      var outTriggerList = data.outTriggerList;
      if (_.isArray(outList)) {
        // 分流试验出组列表
        this.SensorsABTest.out_list = outList;
      }
      if (_.isObject(triggerList)) {
        // 已发命中试验列表
        this.SensorsABTest.triggerList = data.triggerList;
      }
      if (_.isObject(outTriggerList)) {
        // 已发出组试验列表
        this.SensorsABTest.outTriggerList = outTriggerList;
      }
      if (_.isObject(data.track_config)) {
        this.SensorsABTest.track_config = data.track_config;
      } else {
        this.SensorsABTest.track_config = this.SensorsABTest.default_track_config;
      }
    },
    clearExpsCache: function () {
      // 编程试验记录
      this.SensorsABTest.codeExpData = {};
      // 分流试验列表
      this.SensorsABTest.results = [];
      // 分流试验出组列表
      this.SensorsABTest.out_list = [];
      // 已发出组试验列表
      this.SensorsABTest.outTriggerList = {};
      // 已发命中试验列表
      this.SensorsABTest.triggerList = {};
      // 默认配置信息
      this.SensorsABTest.track_config = this.SensorsABTest.default_track_config;
    },
    /**
     * 初始化、页面显示时获取缓存，计算locolstoragetime 如果达到了十分钟则去拉取数据
     * @param {Boolean} isFirst 是否初始化调用
     */
    start: function (isFirst) {
      var last_time = null;
      var now_time = new Date().getTime();
      var data = getStorageData(this.SensorsABTest.state.storage.name);
      var id = this.SensorsABTest.sd.store.getDistinctId();
      if (data && _.isObject(data) && data.distinct_id === id) {
        last_time = data.updateTime;
        if (isFirst) {
          this.updateExpsCache(data);
        }
        if (last_time && _.isNumber(last_time) && now_time - last_time > 0 && now_time - last_time < this.SensorsABTest.para.update_interval) {
          // 使用 update_interval 试验刷新时长。首次打开无需更新时，直接使用缓存
          if (isFirst) {
            this.SensorsABTest.analyzeData();
          }
          this.SensorsABTest.log('数据不更新', last_time, now_time);
          var interval = now_time - last_time;
          var time = this.SensorsABTest.para.update_interval - interval;
          this.setNextFetch(time);
        } else {
          this.SensorsABTest.log('缓存数据超时', last_time, now_time);
          this.getServerData(isFirst);
        }
      } else {
        last_time = this.SensorsABTest.updateTime;
        if (last_time && _.isNumber(last_time) && now_time - last_time > 0 && now_time - last_time < this.SensorsABTest.para.update_interval) {
          this.SensorsABTest.log('数据不更新', last_time, now_time);
        } else {
          this.getServerData(isFirst);
        }
      }
    },
    //页面关闭时，停止拉取数据的计时器
    stop: function () {
      this.SensorsABTest.log('清空拉取定时器');
      this.clearFetchTimer();
    },
    clearFetchTimer: function () {
      clearTimeout(this.timer);
      this.timer = null;
    }
  };

  function Store(SensorsABTest) {
    this.SensorsABTest = SensorsABTest;
  }
  //公共的初始化逻辑
  Store.prototype = {
    init: function (method, context) {
      var _this = this;
      //初始化拉取试验数据
      this.SensorsABTest.fetchData.init(method, context);

      //初始化分流 API
      this.SensorsABTest.initMethods(context);

      //监听页面显示隐藏
      listenPageState({
        visible: function () {
          _this.SensorsABTest.log('页面显示');
          _this.SensorsABTest.fetchData.start();
        },
        hidden: function () {
          _this.SensorsABTest.fetchData.stop();
        }
      });
      //id变化时重新拉取数据
      this.SensorsABTest.sd.events.on('changeDistinctId', function () {
        //
        _this.SensorsABTest.fetchData.clearExpsCache();
        // 还原可视化实验命中结果，重新开始命中
        _this.SensorsABTest.vabtest.restoreElements();
        _this.SensorsABTest.updateStorage();
        _this.SensorsABTest.fetchData.getServerData();
      });

      this.SensorsABTest.sd.events.isReady();
    }
  };
  //入口函数
  SensorsABTest.prototype.init = function (sd, para) {
    //避免重复初始化
    if (this.sd) {
      this.log('A/B Testing SDK 重复初始化！只有第一次初始化有效！');
      return false;
    }
    if ((sd.readyState && sd.readyState.state >= 3) || !sd.on) {
      initSensorsABTest.call(this, sd, para);
    } else {
      var _this = this;
      sd.on('sdkReady', function () {
        initSensorsABTest.call(_this, sd, para);
      });
    }
  };

  function initSensorsABTest(sd, para) {
    //确保JS SDK初始化完成
    if (!getSA(sd)) {
      this.log('A/B Testing 初始化失败,Web JS SDK 没有初始化完成');
      return false;
    }
    this.sd = sd;

    if (!_.isObject(para)) {
      this.log('A/B Testing SDK 初始化失败，请传入正确的初始化参数!para:', para);
      return false;
    }

    //不支持storage无法使用缓存
    if (!_.storage.isSupport()) {
      this.log('localstorage异常');
    }

    this.abtestingPara = para;
    this.fetchData = new FetchData(this);
    this.store = new Store(this);
    this.initTest();
  }

  SensorsABTest.prototype.initTest = function () {
    //初始化多链接试验
    this.link.init(this.abtestingPara.multilink);
    //初始化可视化试验
    this.vabtest.init(this.abtestingPara.visualize);
    //根据打通状态选择不同的初始化模式
    if (this.sd.bridge.is_verify_success) {
      this.bridgeStore.init(this.abtestingPara);
    } else {
      this.normalStore.init(this.abtestingPara);
    }
  };

  SensorsABTest.prototype.log = function () {
    if (_.isString(arguments[0])) {
      arguments[0] = 'sensorsabtest————' + arguments[0];
    }
    return this.sd && this.sd.log.apply(this, arguments);
  };

  var instance = new SensorsABTest();
  instance.__constructor__ = SensorsABTest;

  if (window.SensorsDataWebJSSDKPlugin && Object.prototype.toString.call(window.SensorsDataWebJSSDKPlugin) == '[object Object]') {
    window.SensorsDataWebJSSDKPlugin.SensorsABTest = window.SensorsDataWebJSSDKPlugin.SensorsABTest || instance;
  } else {
    window.SensorsDataWebJSSDKPlugin = {
      SensorsABTest: instance
    };
  }

  return SensorsABTest;

})));
