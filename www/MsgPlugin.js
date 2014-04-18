/*
 * @author Chen Feng
 * */

var exec = require('cordova/exec'),
    cordova = require('cordova');

function  resultHandler (result) {
    console.log("SUCCESS: \r\n"+result );
}

function errorHandler (error) {
    console.log("ERROR: \r\n"+error );
}

function MsgPlugin() {
    init(resultHandler, errorHandler, 'returnSuccess');
}

function init (success, fail, resultType) { 
    exec (success, fail, 
                       "MsgPlugin", 
                       "init", [resultType]); 
};
    
MsgPlugin.prototype.trans = function (str, success, fail) { 
    exec (success, fail, 
                       "MsgPlugin", 
                       "trans", [str]); 

MsgPlugin.prototype.destroy = function (success, fail, resultType) {
    exec (success, fail,
        "MsgPlugin",
        "destroy", [resultType]);
};

module.exports = new MsgPlugin();
