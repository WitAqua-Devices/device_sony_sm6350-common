/*
 * Copyright (C) 2026 The WitAqua Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#pragma once

#include <string>

namespace sony {

/*
 * Reads the marketing name of the device out of the LTALabel partition.
 * Returns an empty string on anything but a japanese model - the global SKUs
 * carry no such name and should keep whatever build.prop already says.
 */
std::string DetectLtaModel();

/* Sets a read-only property from vendor_load_properties(). */
void SetProperty(const std::string& name, const std::string& value);

/*
 * Sets ro.product.model and the per-partition copies of it.
 *
 * Writing ro.product.model here stops property_initialize_ro_product_props()
 * from deriving it later, so the per-partition ones have to be set too or they
 * would still say XQ-BT44.
 */
void SetModelProperties(const std::string& model);

}  // namespace sony
