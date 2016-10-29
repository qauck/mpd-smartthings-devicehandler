/**
 *  Hook
 *
 *  Copyright 2016 Shawn Q.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Hook", namespace: "org.uguess", author: "Shawn Q.") {
		capability "Switch"
    capability "Polling"
	}

	preferences {
		input("token", "string", title:"Oauth Token", description: "token for remote access", required: true, displayDuringSetup: true)
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}

		main "switch"
		details "switch"
	}
}

def on() {
	put('On')
	sendEvent(name: "switch", value: "on")
}

def off() {
	put('Off')
	sendEvent(name: "switch", value: "off")
}

private put(toggle) {
	def params = [
		uri: "https://api.gethook.io/v1/device/trigger/${device.deviceNetworkId}/${toggle}/?token=${token}"
	]
	httpGet(params) 
}