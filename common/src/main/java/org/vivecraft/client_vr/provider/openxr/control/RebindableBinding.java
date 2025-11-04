package org.vivecraft.client_vr.provider.openxr.control;

import org.vivecraft.client_vr.provider.control.ActionType;

public record RebindableBinding(String binding, String path, ActionType type) {
}
