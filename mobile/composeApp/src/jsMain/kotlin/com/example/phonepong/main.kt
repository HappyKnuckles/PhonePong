package com.example.phonepong

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.example.phonepong.ui.requestMotionPermissionHandler
import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import kotlin.js.Promise

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = document.body ?: return

    requestMotionPermissionHandler = { callback ->
        // We inline the JS string to satisfy "constant string" requirement
        val promise = js("""
            (function() {
                if (typeof DeviceMotionEvent !== 'undefined' && typeof DeviceMotionEvent.requestPermission === 'function') {
                    return DeviceMotionEvent.requestPermission()
                        .then(function(motionState) {
                            if (motionState === 'granted') {
                                if (typeof DeviceOrientationEvent !== 'undefined' && typeof DeviceOrientationEvent.requestPermission === 'function') {
                                    return DeviceOrientationEvent.requestPermission();
                                }
                                return 'granted';
                            }
                            throw new Error('Motion permission denied');
                        })
                        .then(function(finalState) {
                             return finalState === 'granted';
                        })
                        .catch(function(e) {
                             console.error(e);
                             return false; 
                        });
                } else {
                    return Promise.resolve(true);
                }
            })()
        """)
        
        // Handle the Promise result
        (promise as Promise<Boolean>).then { success ->
            if (success) {
                js("window.__motionPermissionGranted = true")
            }
            callback(success)
            null
        }.catch { 
            callback(false)
            null 
        }
    }
    // -------------------------------------------------------------
    
    // Check if we require permission logic (iOS 13+)
    val needsPermission = js("""
        (function() {
            return (typeof DeviceMotionEvent !== 'undefined' && typeof DeviceMotionEvent.requestPermission === 'function');
        })()
    """) as Boolean
    
    if (needsPermission) {
        showMotionPermissionUI {
            startComposeApp(body)
        }
    } else {
        js("window.__motionPermissionGranted = true")
        startComposeApp(body)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun startComposeApp(body: org.w3c.dom.HTMLElement) {
    ComposeViewport(body) {
        App()
    }
}

private fun showMotionPermissionUI(onComplete: () -> Unit) {
    val body = document.body ?: return
    
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.id = "motion-permission-overlay"
    overlay.style.cssText = """
        position: fixed; top: 0; left: 0; width: 100%; height: 100%;
        background: linear-gradient(135deg, #1a1a2e 0%, #0f0f1e 100%);
        display: flex; flex-direction: column; justify-content: center; align-items: center;
        z-index: 2147483647; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    """.trimIndent()
    
    overlay.innerHTML = """
        <div style="text-align: center; padding: 32px; max-width: 320px;">
            <div style="font-size: 80px; margin-bottom: 24px;">🎮</div>
            <h1 style="color: white; margin: 0 0 16px 0; font-size: 28px; font-weight: bold;">PhonePong</h1>
            <p style="color: #aaa; margin: 0 0 32px 0; font-size: 16px; line-height: 1.6;">
                To use your phone as a controller, you need to enable motion sensors.
            </p>
            <button id="enable-motion-btn" style="
                background: linear-gradient(135deg, #06D6A0, #00B4D8);
                color: white; border: none; padding: 18px 36px;
                font-size: 18px; font-weight: bold; border-radius: 16px;
                cursor: pointer; width: 100%;
                box-shadow: 0 4px 15px rgba(6, 214, 160, 0.4);
            ">
                Enable Motion Sensors
            </button>
            <p id="permission-status" style="color: #888; margin-top: 16px; font-size: 14px;"></p>
        </div>
    """.trimIndent()
    
    body.appendChild(overlay)
    
    val button = document.getElementById("enable-motion-btn") as? HTMLButtonElement
    
    button?.onclick = {
        // FIX: Cast to HTMLElement to access .style and .textContent
        val statusEl = document.getElementById("permission-status") as? HTMLElement
        statusEl?.textContent = "Requesting permission..."
        
        // Execute the handler defined at the top of main()
        requestMotionPermissionHandler { success ->
            if (success) {
                document.getElementById("motion-permission-overlay")?.remove()
                js("window.__onPermissionGranted()")
            } else {
                statusEl?.textContent = "❌ Permission denied. Check Safari Settings."
                // This line now works because statusEl is cast to HTMLElement
                statusEl?.style?.color = "#ff6b6b"
            }
        }
    }
    
    js("window.__onPermissionGranted = arguments[0]")(onComplete)
}