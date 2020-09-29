
import 'package:flutter/cupertino.dart';

class SsidInfo {
  var ssid = "";
  var bssid = "";
}

/// wifi ssid text input
/// form field
///

class WifiInputFormField extends FormField<SsidInfo> {
  final String titleText;
  final String hintText;
  final bool required;
  final String errorText;
  final SsidInfo value;
  final List dataSource;
  final String textField;
  final String valueField;
  final Function onChanged;
  final bool filled;
  final EdgeInsets contentPadding;

  WifiInputFormField(
  {FormFieldSetter<dynamic> onSaved,
  FormFieldValidator<dynamic> validator,
  bool autovalidate = false,
  this.titleText = 'Title',
  this.hintText = 'Select one option',
  this.required = false,
  this.errorText = 'Please select one option',
  this.value,
  this.dataSource,
  this.textField,
  this.valueField,
  this.onChanged,
  this.filled = true,
  this.contentPadding = const EdgeInsets.fromLTRB(12, 12, 8, 0)})
      :super(
    onSaved: onSaved,
    validator: validator,
    autovalidate: autovalidate,
    builder: (FormFieldState<SsidInfo> state) {
      return Container();
  }
  );
}
