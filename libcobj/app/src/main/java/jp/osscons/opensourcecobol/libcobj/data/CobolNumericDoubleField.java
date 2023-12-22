/*
 * Copyright (C) 2021-2022 TOKYO SYSTEM HOUSE Co., Ltd.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3.0,
 * or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; see the file COPYING.LIB.  If
 * not, write to the Free Software Foundation, 51 Franklin Street, Fifth Floor
 * Boston, MA 02110-1301 USA
 */
package jp.osscons.opensourcecobol.libcobj.data;

import java.math.BigDecimal;
import jp.osscons.opensourcecobol.libcobj.exceptions.CobolRuntimeException;

public class CobolNumericDoubleField extends AbstractCobolField {

  public CobolNumericDoubleField(
      int size, CobolDataStorage dataStorage, CobolFieldAttribute attribute) {
    super(size, dataStorage, attribute);
  }

  private double getBinaryValue() {
    CobolDataStorage storage = this.getDataStorage();
    return storage.doubleValue();
  }

  @Override
  public byte[] getBytes() {
    return new byte[0];
  }

  @Override
  public String getString() {
    return String.format("%.18f", this.getDouble());
  }

  @Override
  public int getSign() {
    double n = this.getBinaryValue();

    if (n < 0) {
      return -1;
    } else if (n > 0) {
      return 1;
    } else {
      return 0;
    }
  }

  @Override
  public double getDouble() {
    return this.getBinaryValue();
  }

  @Override
  public void setDecimal(BigDecimal decimal) {
    // TODO Auto-generated method stub
  }

  @Override
  public int addPackedInt(int n) {
    throw new CobolRuntimeException(0, "実装しないコード");
  }

  @Override
  public void moveFrom(AbstractCobolField src) {
    AbstractCobolField src1 = this.preprocessOfMoving(src);
    if (src1 == null) {
      return;
    }
    switch (src1.getAttribute().getType()) {
      case CobolFieldAttribute.COB_TYPE_NUMERIC_DISPLAY:
        this.moveDisplayToDouble(src1);
        break;
      case CobolFieldAttribute.COB_TYPE_NUMERIC_PACKED:
      case CobolFieldAttribute.COB_TYPE_ALPHANUMERIC:
      case CobolFieldAttribute.COB_TYPE_ALPHANUMERIC_EDITED:
      case CobolFieldAttribute.COB_TYPE_NUMERIC_BINARY:
      case CobolFieldAttribute.COB_TYPE_NUMERIC_DOUBLE:
      case CobolFieldAttribute.COB_TYPE_NUMERIC_FLOAT:
      case CobolFieldAttribute.COB_TYPE_NATIONAL:
      case CobolFieldAttribute.COB_TYPE_NUMERIC_EDITED:
      case CobolFieldAttribute.COB_TYPE_NATIONAL_EDITED:
        this.moveFrom(src1.getNumericField());
        break;
      case CobolFieldAttribute.COB_TYPE_GROUP:
        CobolAlphanumericField.moveAlphanumToAlphanum(this, src1);
        break;
      default:
        throw new CobolRuntimeException(0, "未実装");
    }
  }

  public void moveDisplayToDouble(AbstractCobolField src) {
    double dval = 0;
    BigDecimal d1 = BigDecimal.ZERO;
    BigDecimal d2;
    CobolDataStorage storage = src.getDataStorage();
    for (int i = 0; i < src.getSize(); ++i) {
      byte c = storage.getByte(i);
      if (c == (byte) '-' || c == (byte) '+') {
        continue;
      }
      // dval *= 10;
      // dval += c >= 0x70 ? c - 0x70 : c - 0x30;
      d1 = d1.multiply(BigDecimal.TEN);
      d1 = d1.add(new BigDecimal(c >= 0x70 ? c - 0x70 : c - 0x30));
    }
    int scale = src.getAttribute().getScale();
    for (int i = 0; i < Math.abs(scale); ++i) {
      if (scale < 0) {
        // dval *= 10;
        d1 = d1.multiply(BigDecimal.TEN);
      } else {
        // dval /= 10;
        d1 = d1.divide(BigDecimal.TEN);
      }
    }
    BigDecimal mONE = new BigDecimal(-1);
    if (src.getSign() < 0) {
      // dval *= -1;
      d1 = d1.multiply(mONE);
    }
    // this.getDataStorage().set(dval);
    CobolDecimal d3 = new CobolDecimal(d1);
    // this.setDataStorage(new CobolDataStorage(d1.toPlainString().getBytes()));
    this.getDataStorage().set(d3.decimalGetDouble());
  }

  @Override
  public CobolNumericField getNumericField() {
    CobolFieldAttribute attr =
        new CobolFieldAttribute(
            CobolFieldAttribute.COB_TYPE_NUMERIC_DISPLAY,
            40,
            20,
            CobolFieldAttribute.COB_FLAG_HAVE_SIGN,
            null);
    CobolDataStorage storage = new CobolDataStorage(40);
    double dval = Math.abs(this.getBinaryValue());
    String dataString = String.format("%041.20f", dval).replace(".", "");
    storage.memcpy(dataString, 40);
    AbstractCobolField field = CobolFieldFactory.makeCobolField(40, storage, attr);
    if (this.getSign() >= 0) {
      field.putSign(1);
    } else {
      field.putSign(-1);
    }
    return (CobolNumericField) field;
  }

  @Override
  public CobolDecimal getDecimal() {
    return new CobolDecimal(BigDecimal.valueOf(this.getBinaryValue()), 0);
  }

  @Override
  public void moveFrom(byte[] bytes) {}

  @Override
  public void moveFrom(String string) {}

  @Override
  public void moveFrom(int number) {}

  @Override
  public void moveFrom(double number) {}

  @Override
  public void moveFrom(BigDecimal number) {}

  @Override
  public void moveFrom(CobolDataStorage dataStrage) {}
}
