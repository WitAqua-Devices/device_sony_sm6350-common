#!/usr/bin/env -S PYTHONPATH=../../../tools/extract-utils python3
#
# SPDX-FileCopyrightText: The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#
# device/sony/sm6350-common/extract-files.py
#
# Usage, from inside the build tree:
#   ./extract-files.py <path to an extracted stock firmware>

from extract_utils.fixups_blob import (
    blob_fixup,
    blob_fixups_user_type,
)
from extract_utils.fixups_lib import (
    lib_fixups,
    lib_fixups_user_type,
)
from extract_utils.main import (
    ExtractUtils,
    ExtractUtilsModule,
)

# Namespaces holding the source modules the blobs depend on.  Missing one
# makes soong fail with
#   "libwfddisplayconfig" depends on undefined module "libdisplayconfig.system.qti"
namespace_imports = [
    'device/sony/sm6350-common',
    'hardware/qcom-caf/sm8250',
    'hardware/qcom-caf/wlan',
    'hardware/sony',
    'vendor/qcom/opensource/commonsys/display',
    'vendor/qcom/opensource/commonsys-intf/display',
    'vendor/qcom/opensource/dataservices',
    'vendor/qcom/opensource/display',
]

def lib_fixup_vendor_suffix(lib: str, partition: str, *args, **kwargs):
    return f'{lib}_{partition}' if partition == 'vendor' else None


# Libraries that exist in both system_ext/lib64 and vendor/lib64.  Modules are
# named after the basename, so the vendor copy needs a _vendor suffix to keep
# soong from seeing the same module twice.
lib_fixups: lib_fixups_user_type = {
    **lib_fixups,
    (
        'com.qualcomm.qti.dpm.api@1.0',
        'com.qualcomm.qti.imscmservice@1.0',
        'com.qualcomm.qti.imscmservice@2.0',
        'com.qualcomm.qti.imscmservice@2.1',
        'com.qualcomm.qti.imscmservice@2.2',
        'com.qualcomm.qti.uceservice@2.0',
        'com.qualcomm.qti.uceservice@2.1',
        'libmmosal',
        'vendor.qti.hardware.data.cne.internal.api@1.0',
        'vendor.qti.hardware.data.cne.internal.constants@1.0',
        'vendor.qti.hardware.data.cne.internal.server@1.0',
        'vendor.qti.hardware.data.connection@1.0',
        'vendor.qti.hardware.data.connection@1.1',
        'vendor.qti.hardware.data.dynamicdds@1.0',
        'vendor.qti.hardware.data.iwlan@1.0',
        'vendor.qti.hardware.data.qmi@1.0',
        'vendor.qti.hardware.fm@1.0',
        'vendor.qti.hardware.qseecom@1.0',
        'vendor.qti.hardware.tui_comm@1.0',
        'vendor.qti.hardware.wifidisplaysession@1.0',
        'vendor.qti.ims.callinfo@1.0',
        'vendor.qti.ims.rcsconfig@1.0',
        'vendor.qti.ims.rcsconfig@1.1',
        'vendor.qti.imsrtpservice@3.0',
        'vendor.somc.hardware.miscta@1.0',
        'vendor.somc.hardware.security.secd@1.1',
    ): lib_fixup_vendor_suffix,
}

# Point the Android 11 era blobs at the names the current tree uses.  The AIDL
# NDK backend libraries were renamed from -V1-ndk_platform to -V1-ndk, and the
# old names make soong fail with "depends on undefined module".
blob_fixups: blob_fixups_user_type = {
    (
        'vendor/lib64/libcammw.so',
        'vendor/lib64/vendor.semc.hardware.extlight-V1-ndk_platform.so',
    ): blob_fixup()
        .replace_needed(
            'android.hardware.light-V1-ndk_platform.so',
            'android.hardware.light-V1-ndk.so',
        ),
    # android::base::Trim() used to be a plain function taking a
    # const std::string&.  It is a template over string-like types now, so the
    # old mangled name is gone and libnfc_shim brings it back.
    (
        'vendor/lib64/ese_spi_nxp.so',
        'vendor/lib64/nfc_nci_nxp.so',
    ): blob_fixup()
        .add_needed('libnfc_shim.so'),
    # libwvhidl uses the BoringSSL CBS API, which the current libcrypto no
    # longer exports to vendor.  libcrypto_shim from hardware/lineage/compat
    # provides it.
    (
        'vendor/lib/mediadrm/libwvdrmengine.so',
        'vendor/lib/libwvhidl.so',
        'vendor/lib64/mediadrm/libwvdrmengine.so',
        'vendor/lib64/libwvhidl.so',
    ): blob_fixup()
        .add_needed('libcrypto_shim.so'),
}  # fmt: skip

module = ExtractUtilsModule(
    'sm6350-common',
    'sony',
    namespace_imports=namespace_imports,
    blob_fixups=blob_fixups,
    lib_fixups=lib_fixups,
)

if __name__ == '__main__':
    utils = ExtractUtils.device(module)
    utils.run()
