

module.exports = (function() {

  var _status = function (successCallback, errorCallback) {
      cordova.exec(successCallback, errorCallback, "Gpsstatus", "gpsStatus", []);
    };
  var _opengps =function (successCallback, errorCallback) {
      cordova.exec(successCallback, errorCallback, "Gpsstatus", "openGps", []);
    };
  
   return {
    StatusGps: _status,
    openGps: _opengps,
    subscribe:_subscribe,
    
     


















  };

})();
