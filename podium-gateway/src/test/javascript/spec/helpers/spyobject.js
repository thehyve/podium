"use strict";
var SpyObject = (function () {
    function SpyObject(type) {
        var _this = this;
        if (type === void 0) {
            type = null;
        }
        if (type) {
            Object.keys(type.prototype).forEach(function (prop) {
                var m = null;
                try {
                    m = type.prototype[prop];
                }
                catch (e) {
                }
                if (typeof m === 'function') {
                    _this.spy(prop);
                }
            });
        }
    }

    SpyObject.stub = function (object, config, overrides) {
        if (object === void 0) {
            object = null;
        }
        if (config === void 0) {
            config = null;
        }
        if (overrides === void 0) {
            overrides = null;
        }
        if (!(object instanceof SpyObject)) {
            overrides = config;
            config = object;
            object = new SpyObject();
        }
        var m = {};
        Object.keys(config).forEach(function (key) {
            return m[key] = config[key];
        });
        Object.keys(overrides).forEach(function (key) {
            return m[key] = overrides[key];
        });
        Object.keys(m).forEach(function (key) {
            object.spy(key).andReturn(m[key]);
        });
        return object;
    };
    SpyObject.prototype.spy = function (name) {
        if (!this[name]) {
            this[name] = this._createGuinnessCompatibleSpy(name);
        }
        return this[name];
    };
    SpyObject.prototype.prop = function (name, value) {
        this[name] = value;
    };
    /** @internal */
    SpyObject.prototype._createGuinnessCompatibleSpy = function (name) {
        var newSpy = jasmine.createSpy(name);
        newSpy.andCallFake = newSpy.and.callFake;
        newSpy.andReturn = newSpy.and.returnValue;
        newSpy.reset = newSpy.calls.reset;
        // revisit return null here (previously needed for rtts_assert).
        newSpy.and.returnValue(null);
        return newSpy;
    };
    return SpyObject;
}());
exports.SpyObject = SpyObject;
