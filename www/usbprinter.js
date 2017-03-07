module.exports = (function() {
  var _print = function (args,successCallback, errorCallback) {
      cordova.exec(successCallback, errorCallback, "Print", "printData", [args]);
    };
   return {
    Print: _print
    };
})();
