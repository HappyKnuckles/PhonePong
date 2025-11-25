package com.example.insanecrossmobilepingpongapp.controller

import com.example.insanecrossmobilepingpongapp.model.PaddleControl

/**
 * Interface for receiving paddle control updates.
 * Implement this interface to handle control data (e.g., send via WebSocket).
 */
interface ControllerOutput {
    /**
     * Called when paddle control values change.
     *
     * @param control The updated paddle control values
     */
    fun onControlChanged(control: PaddleControl)
}
