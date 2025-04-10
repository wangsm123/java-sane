package com.sane.java;

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import java.util.Arrays;
import java.util.List;

/**
 * author wangsm
 * date 2025-04-10
 */
public interface SaneInterface extends Library {

    SaneInterface INSTANCE = Native.load("sane", SaneInterface.class);

    // SANE types and defines
    int SANE_CURRENT_MAJOR = 1;
    int SANE_CURRENT_MINOR = 0;

    int SANE_FALSE = 0;
    int SANE_TRUE = 1;

    // SANE_Status enum
    int SANE_STATUS_GOOD = 0;
    int SANE_STATUS_UNSUPPORTED = 1;
    int SANE_STATUS_CANCELLED = 2;
    int SANE_STATUS_DEVICE_BUSY = 3;
    int SANE_STATUS_INVAL = 4;
    int SANE_STATUS_EOF = 5;
    int SANE_STATUS_JAMMED = 6;
    int SANE_STATUS_NO_DOCS = 7;
    int SANE_STATUS_COVER_OPEN = 8;
    int SANE_STATUS_IO_ERROR = 9;
    int SANE_STATUS_NO_MEM = 10;
    int SANE_STATUS_ACCESS_DENIED = 11;

    // SANE_Value_Type enum
    int SANE_TYPE_BOOL = 0;
    int SANE_TYPE_INT = 1;
    int SANE_TYPE_FIXED = 2;
    int SANE_TYPE_STRING = 3;
    int SANE_TYPE_BUTTON = 4;
    int SANE_TYPE_GROUP = 5;

    // SANE_Unit enum
    int SANE_UNIT_NONE = 0;
    int SANE_UNIT_PIXEL = 1;
    int SANE_UNIT_BIT = 2;
    int SANE_UNIT_MM = 3;
    int SANE_UNIT_DPI = 4;
    int SANE_UNIT_PERCENT = 5;
    int SANE_UNIT_MICROSECOND = 6;

    // SANE_Action enum
    int SANE_ACTION_GET_VALUE = 0;
    int SANE_ACTION_SET_VALUE = 1;
    int SANE_ACTION_SET_AUTO = 2;

    // SANE_Frame enum
    int SANE_FRAME_GRAY = 0;
    int SANE_FRAME_RGB = 1;
    int SANE_FRAME_RED = 2;
    int SANE_FRAME_GREEN = 3;
    int SANE_FRAME_BLUE = 4;

    class SANE_Device extends Structure {
        public String name;
        public String vendor;
        public String model;
        public String type;
        public SANE_Device(Pointer p) {
            super(p);
            read();
        }

        @Override
        protected List<String> getFieldOrder() {
            // TODO Auto-generated method stub
            return Arrays.asList(new String[] { "name", "vendor", "model", "type" });
        }
        public static class ByReference extends SANE_Device implements Structure.ByReference {

            public ByReference(Pointer p) {
                super(p);
                // TODO Auto-generated constructor stub
            }}
    }

    class SaneParameters extends Structure {
        public int format;
        public int last_frame;
        public int bytes_per_line;
        public int pixels_per_line;
        public int lines;
        public int depth;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("format", "last_frame", "bytes_per_line", "pixels_per_line", "lines", "depth");
        }
    }
    class SANE_Option_Descriptor extends Structure {
        public static class ByReference extends SANE_Option_Descriptor implements Structure.ByReference {}

        public String name;        // name of this option (command-line name)
        public String title;       // title of this option (single-line)
        public String desc;        // description of this option (multi-line)
        public int type;           // how are values interpreted? (SANE_Value_Type)
        public int unit;           // what is the (physical) unit? (SANE_Unit)
        public int size;           // size of the value
        public int cap;            // capabilities

        public int constraint_type; // SANE_Constraint_Type
        public Union constraint;   // union for constraint

        public static class Union extends com.sun.jna.Union {
            public Pointer string_list;  // NULL-terminated list of strings
            public Pointer word_list;    // first element is list-length
            public Pointer range;        // SANE_Range
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("name", "title", "desc", "type", "unit", "size", "cap", "constraint_type", "constraint");
        }
    }

    class SANE_Range extends Structure {
        public int min;
        public int max;
        public int quant;

        public SANE_Range(Pointer p) {
            super(p);
            read();
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("min", "max", "quant");
        }
    }




    interface SANE_Auth_Callback extends Callback {
        void invoke(String resource, Pointer username, Pointer password);
    }

    int sane_init(IntByReference version_code, SANE_Auth_Callback authorize);
    void sane_exit();
    int sane_get_devices(PointerByReference device_list, int local_only);
    int sane_open(String deviceName, PointerByReference handle);
    void sane_close(Pointer handle);
    SANE_Option_Descriptor sane_get_option_descriptor(Pointer handle, int option);
    int sane_control_option(Pointer handle, int option, int action, Pointer value, IntByReference info);
    int sane_get_parameters(Pointer handle, SaneParameters params);
    int sane_start(Pointer handle);
    int sane_read(Pointer handle, byte[] data, int maxLength, IntByReference length);
    void sane_cancel(Pointer handle);
    int sane_set_io_mode(Pointer handle, int nonBlocking);
    int sane_get_select_fd(Pointer handle, IntByReference fd);
    String sane_strstatus(int status);
}
