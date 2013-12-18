/*
 * @author Chen Feng
 * */

var exec = require('cordova/exec'),
    cordova = require('cordova');

function MsgPlugin() {
    init(resultHandler, errorHandler, 'returnSuccess');
}

function init (success, fail, resultType) { 
    return cordova.exec (success, fail, 
                       "MsgPlugin", 
                       "init", [resultType]); 
};
    
MsgPlugin.prototype.trans = function (success, fail, resultType) { 
    return cordova.exec (success, fail, 
                       "MsgPlugin", 
                       "trans", [resultType]); 
}; 

module.exports = new MsgPlugin();
