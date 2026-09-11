/*
 * Copyright (C) 2026 The WitAqua Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#include <android-base/logging.h>

#include "sku.h"

/*
 * One build covers every japanese SKU, so the model has to come from the
 * hardware. LTALabel is the only thing on the device that carries it.
 *
 * ro.vendor.ltalabel.model is published here rather than from an init script:
 * init.sony.rc binds the per-model FeliCa configuration in early-init, and a
 * bind mount put in place any later no longer reaches file descriptors that
 * are already open. sm8250 gets the same property out of the odm msim script,
 * which this device does not ship.
 *
 * This runs before the fstab is used for anything, but the LTALabel entry in
 * it is still what makes the read possible: first stage init only creates the
 * by-name node for partitions the fstab mentions.
 */
void vendor_load_properties() {
    std::string model = sony::DetectLtaModel();
    if (model.empty()) {
        LOG(INFO) << "libinit: no japanese model in LTALabel, keeping the built-in model";
        return;
    }

    LOG(INFO) << "libinit: detected " << model;
    sony::SetModelProperties(model);
    sony::SetProperty("ro.vendor.ltalabel.model", model);
}
