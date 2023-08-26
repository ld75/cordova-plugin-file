class ButtonHandler extends Widget {

  get _nativeType() {
    return 'com.ButtonHandler';
  }
set maxLines(value) {
    this._nativeSet('maxLines', value);
  }

  get maxLines() {
    return this._nativeGet('maxLines');
  }
 _listen(name, listening) {
    if (name === 'myEvent') {
      this._nativeListen(name, listening);
    } else {
      super._listen(name, listening);
    }
  }
}