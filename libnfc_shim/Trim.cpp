/*
 * Copyright (C) 2026 The WitAqua Project
 * SPDX-License-Identifier: Apache-2.0
 */

#include <android-base/strings.h>

#include <string>
#include <string_view>

namespace android {
namespace base {

/*
 * ese_spi_nxp.so was built against the libbase of Android 12, where Trim()
 * took a const std::string&.  It is a template now, so the old mangled name
 * is gone and the stock NFC and eSE HALs fail to link.  The call below picks
 * the template, so this does not recurse.
 */
std::string Trim(const std::string& s) {
    return Trim(std::string_view(s));
}

}  // namespace base
}  // namespace android
