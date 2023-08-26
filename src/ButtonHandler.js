class ButtonHandler extends Widget {

  get _nativeType() {
    return 'myLibrary.MyCustomWidget';
  }
set myProperty(value) {
    this._nativeSet('myProperty', value);
  }

  get myProperty() {
    return this._nativeGet('myProperty');
  }
 _listen(name, listening) {
    if (name === 'myEvent') {
      this._nativeListen(name, listening);
    } else {
      super._listen(name, listening);
    }
  }
}