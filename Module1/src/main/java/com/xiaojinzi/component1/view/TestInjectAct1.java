package com.xiaojinzi.component1.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.xiaojinzi.base.ModuleConfig;
import com.xiaojinzi.base.view.BaseAct;
import com.xiaojinzi.component.Component;
import com.xiaojinzi.component.anno.FieldAutowiredAnno;
import com.xiaojinzi.component.anno.RouterAnno;
import com.xiaojinzi.component1.R;

/**
 * 测试属性注入功能的界面
 */
@RouterAnno(
        path = ModuleConfig.Module1.TEST_INJECT1
)
public class TestInjectAct1 extends BaseAct {

    public static final int defaultValue = -1;

    @FieldAutowiredAnno("valueString")
    String valueString;

    @FieldAutowiredAnno("valueStringDefault")
    String valueStringDefault = "hello";

    @FieldAutowiredAnno("valueByte")
    byte valueByte;
    @FieldAutowiredAnno("valueByteDefalut")
    byte valueByteDefalut = defaultValue;
    @FieldAutowiredAnno("valueByteBox")
    Byte valueByteBox;
    @FieldAutowiredAnno("valueByteBoxDefalut")
    Byte valueByteBoxDefalut = defaultValue;

    @FieldAutowiredAnno("valueShort")
    short valueShort;
    @FieldAutowiredAnno("valueShortDefalut")
    short valueShortDefalut = defaultValue;
    @FieldAutowiredAnno("valueShortBox")
    Short valueShortBox;
    @FieldAutowiredAnno("valueShortBoxDefalut")
    Short valueShortBoxDefalut = defaultValue;

    @FieldAutowiredAnno("valueInt")
    int valueInt;
    @FieldAutowiredAnno("valueIntDefalut")
    int valueIntDefalut = defaultValue;
    @FieldAutowiredAnno("valueIntBox")
    Integer valueIntBox;
    @FieldAutowiredAnno("valueIntBoxDefalut")
    Integer valueIntBoxDefalut = defaultValue;

    @FieldAutowiredAnno("valueLong")
    long valueLong;
    @FieldAutowiredAnno("valueLongDefalut")
    long valueLongDefalut = defaultValue;
    @FieldAutowiredAnno("valueLongBox")
    Long valueLongBox;
    @FieldAutowiredAnno("valueLongBoxDefalut")
    Long valueLongBoxDefalut = Long.valueOf(defaultValue);

    @FieldAutowiredAnno("valueFloat")
    float valueFloat;
    @FieldAutowiredAnno("valueFloatDefalut")
    float valueFloatDefalut = defaultValue;
    @FieldAutowiredAnno("valueFloatBox")
    Float valueFloatBox;
    @FieldAutowiredAnno("valueFloatBoxDefalut")
    Float valueFloatBoxDefalut = Float.valueOf(defaultValue);

    @FieldAutowiredAnno("valueDouble")
    double valueDouble;
    @FieldAutowiredAnno("valueDoubleDefalut")
    double valueDoubleDefalut = defaultValue;
    @FieldAutowiredAnno("valueDoubleBox")
    Double valueDoubleBox;
    @FieldAutowiredAnno("valueDoubleBoxDefalut")
    Double valueDoubleBoxDefalut = Double.valueOf(defaultValue);

    @FieldAutowiredAnno("valueBoolean")
    boolean valueBoolean;
    @FieldAutowiredAnno("valueBooleanDefalut")
    boolean valueBooleanDefalut = true;
    @FieldAutowiredAnno("valueBooleanBox")
    Boolean valueBooleanBox;
    @FieldAutowiredAnno("valueBooleanBoxDefalut")
    Boolean valueBooleanBoxDefalut = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.component1_test_inject_parameter_act);
        Component.inject(this);
    }

    @Override
    protected void returnData() {
        if (check()) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Intent intent = new Intent();
            setResult(RESULT_ERROR, intent);
            finish();
        }
    }

    private boolean check() {

        Bundle bundle = getIntent().getExtras();

        // 检查 String 参数的 =============================================

        if (bundle.containsKey("valueString")) {
            if (!valueString.equals(bundle.getString("valueString"))) {
                return false;
            }
        } else {
            if (valueString != null) {
                return false;
            }
        }
        if (bundle.containsKey("valueStringDefault")) {
            if (!valueStringDefault.equals(bundle.getString("valueStringDefault"))) {
                return false;
            }
        } else {
            if (!"hello".equals(valueStringDefault)) {
                return false;
            }
        }

        // 检查 Byte 参数的 ======================================================
        // 如果存在
        if (bundle.containsKey("valueByte")) {
            if (valueByte != bundle.getByte("valueByte")) {
                return false;
            }
        } else {
            if (valueByte != 0) {
                return false;
            }
        }

        if (bundle.containsKey("valueByteDefalut")) {
            if (valueByteDefalut != bundle.getByte("valueByteDefalut")) {
                return false;
            }
        } else {
            if (valueByteDefalut != defaultValue) {
                return false;
            }
        }

        if (bundle.containsKey("valueByteBox")) {
            if (valueByteBox == null) {
                return false;
            } else {
                if (!valueByteBox.equals(bundle.getByte("valueByteBox"))) {
                    return false;
                }
            }
        } else {
            if (valueByteBox != null) {
                return false;
            }
        }

        if (bundle.containsKey("valueByteBoxDefalut")) {
            if (valueByteBoxDefalut == null || valueByteBoxDefalut == defaultValue) {
                return false;
            } else {
                if (valueByteBoxDefalut != bundle.getByte("valueByteBoxDefalut")) {
                    return false;
                }
            }
        } else {
            if (valueByteBoxDefalut != defaultValue) {
                return false;
            }
        }

        // 检查 Short 参数的 ======================================================
        // 如果存在
        if (bundle.containsKey("valueShort")) {
            if (valueShort != bundle.getShort("valueShort")) {
                return false;
            }
        } else {
            if (valueShort != 0) {
                return false;
            }
        }

        if (bundle.containsKey("valueShortDefalut")) {
            if (valueShortDefalut != bundle.getShort("valueShortDefalut")) {
                return false;
            }
        } else {
            if (valueShortDefalut != defaultValue) {
                return false;
            }
        }

        if (bundle.containsKey("valueShortBox")) {
            if (valueShortBox == null) {
                return false;
            } else {
                if (!valueShortBox.equals(bundle.getShort("valueShortBox"))) {
                    return false;
                }
            }
        } else {
            if (valueShortBox != null) {
                return false;
            }
        }

        if (bundle.containsKey("valueShortBoxDefalut")) {
            if (valueShortBoxDefalut == null || valueShortBoxDefalut == defaultValue) {
                return false;
            } else {
                if (valueShortBoxDefalut != bundle.getShort("valueShortBoxDefalut")) {
                    return false;
                }
            }
        } else {
            if (valueShortBoxDefalut != defaultValue) {
                return false;
            }
        }

        // 检查 Int 参数的 ======================================================
        // 如果存在
        if (bundle.containsKey("valueInt")) {
            if (valueInt != bundle.getInt("valueInt")) {
                return false;
            }
        } else {
            if (valueInt != 0) {
                return false;
            }
        }

        if (bundle.containsKey("valueIntDefalut")) {
            if (valueIntDefalut != bundle.getInt("valueIntDefalut")) {
                return false;
            }
        } else {
            if (valueIntDefalut != defaultValue) {
                return false;
            }
        }

        if (bundle.containsKey("valueIntBox")) {
            if (valueIntBox == null) {
                return false;
            } else {
                if (!valueIntBox.equals(bundle.getInt("valueIntBox"))) {
                    return false;
                }
            }
        } else {
            if (valueIntBox != null) {
                return false;
            }
        }

        if (bundle.containsKey("valueIntBoxDefalut")) {
            if (valueIntBoxDefalut == null || valueIntBoxDefalut == defaultValue) {
                return false;
            } else {
                if (valueIntBoxDefalut != bundle.getInt("valueIntBoxDefalut")) {
                    return false;
                }
            }
        } else {
            if (valueIntBoxDefalut != defaultValue) {
                return false;
            }
        }

        // 检查 Long 参数的 ======================================================
        // 如果存在
        if (bundle.containsKey("valueLong")) {
            if (valueLong != bundle.getLong("valueLong")) {
                return false;
            }
        } else {
            if (valueLong != 0) {
                return false;
            }
        }

        if (bundle.containsKey("valueLongDefalut")) {
            if (valueLongDefalut != bundle.getLong("valueLongDefalut")) {
                return false;
            }
        } else {
            if (valueLongDefalut != defaultValue) {
                return false;
            }
        }

        if (bundle.containsKey("valueLongBox")) {
            if (valueLongBox == null) {
                return false;
            } else {
                if (!valueLongBox.equals(bundle.getLong("valueLongBox"))) {
                    return false;
                }
            }
        } else {
            if (valueLongBox != null) {
                return false;
            }
        }

        if (bundle.containsKey("valueLongBoxDefalut")) {
            if (valueLongBoxDefalut == null || valueLongBoxDefalut == defaultValue) {
                return false;
            } else {
                if (valueLongBoxDefalut != bundle.getLong("valueLongBoxDefalut")) {
                    return false;
                }
            }
        } else {
            if (valueLongBoxDefalut != defaultValue) {
                return false;
            }
        }

        // 检查 Float 参数的 ======================================================
        // 如果存在
        if (bundle.containsKey("valueFloat")) {
            if (valueFloat != bundle.getFloat("valueFloat")) {
                return false;
            }
        } else {
            if (valueFloat != 0) {
                return false;
            }
        }

        if (bundle.containsKey("valueFloatDefalut")) {
            if (valueFloatDefalut != bundle.getFloat("valueFloatDefalut")) {
                return false;
            }
        } else {
            if (valueFloatDefalut != defaultValue) {
                return false;
            }
        }

        if (bundle.containsKey("valueFloatBox")) {
            if (valueFloatBox == null) {
                return false;
            } else {
                if (!valueFloatBox.equals(bundle.getFloat("valueFloatBox"))) {
                    return false;
                }
            }
        } else {
            if (valueFloatBox != null) {
                return false;
            }
        }

        if (bundle.containsKey("valueFloatBoxDefalut")) {
            if (valueFloatBoxDefalut == null || valueFloatBoxDefalut == defaultValue) {
                return false;
            } else {
                if (valueFloatBoxDefalut != bundle.getFloat("valueFloatBoxDefalut")) {
                    return false;
                }
            }
        } else {
            if (valueFloatBoxDefalut != defaultValue) {
                return false;
            }
        }

        // 检查 Double 参数的 ======================================================
        // 如果存在
        if (bundle.containsKey("valueDouble")) {
            if (valueDouble != bundle.getDouble("valueDouble")) {
                return false;
            }
        } else {
            if (valueDouble != 0) {
                return false;
            }
        }

        if (bundle.containsKey("valueDoubleDefalut")) {
            if (valueDoubleDefalut != bundle.getDouble("valueDoubleDefalut")) {
                return false;
            }
        } else {
            if (valueDoubleDefalut != defaultValue) {
                return false;
            }
        }

        if (bundle.containsKey("valueDoubleBox")) {
            if (valueDoubleBox == null) {
                return false;
            } else {
                if (!valueDoubleBox.equals(bundle.getDouble("valueDoubleBox"))) {
                    return false;
                }
            }
        } else {
            if (valueDoubleBox != null) {
                return false;
            }
        }

        if (bundle.containsKey("valueDoubleBoxDefalut")) {
            if (valueDoubleBoxDefalut == null || valueDoubleBoxDefalut == defaultValue) {
                return false;
            } else {
                if (valueDoubleBoxDefalut != bundle.getDouble("valueDoubleBoxDefalut")) {
                    return false;
                }
            }
        } else {
            if (valueDoubleBoxDefalut != defaultValue) {
                return false;
            }
        }

        // 检查 Boolean 参数的 ======================================================
        // 如果存在
        if (bundle.containsKey("valueBoolean")) {
            if (valueBoolean != bundle.getBoolean("valueBoolean")) {
                return false;
            }
        } else {
            if (valueBoolean != false) {
                return false;
            }
        }

        if (bundle.containsKey("valueBooleanDefalut")) {
            if (valueBooleanDefalut != bundle.getBoolean("valueBooleanDefalut")) {
                return false;
            }
        } else {
            if (valueBooleanDefalut != true) {
                return false;
            }
        }

        if (bundle.containsKey("valueBooleanBox")) {
            if (valueBooleanBox == null) {
                return false;
            } else {
                if (!valueBooleanBox.equals(bundle.getBoolean("valueBooleanBox"))) {
                    return false;
                }
            }
        } else {
            if (valueBooleanBox != null) {
                return false;
            }
        }

        if (bundle.containsKey("valueBooleanBoxDefalut")) {
            if (valueBooleanBoxDefalut == null || valueBooleanBoxDefalut == true) {
                return false;
            } else {
                if (valueBooleanBoxDefalut != bundle.getBoolean("valueBooleanBoxDefalut")) {
                    return false;
                }
            }
        } else {
            if (valueBooleanBoxDefalut != true) {
                return false;
            }
        }

        return true;

    }

}
