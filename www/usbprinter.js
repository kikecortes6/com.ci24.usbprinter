module.exports = (function() {
  var _print = function (args,successCallback, errorCallback) {
      cordova.exec(successCallback, errorCallback, "Printer", "printData", [args]);
    };
  var _feedPaper = function (successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Printer", "feedPaper", []);
  };
  var _openPrinter = function (successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Printer", "openPrinter", []);
  };
   return {
    Print: _print,
     FeedPaper:_feedPaper,
     OpenPrinter:_openPrinter
    };
})();
