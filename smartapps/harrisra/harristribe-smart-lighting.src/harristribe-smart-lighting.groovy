/**
 *  HarrisTribe Smart Lighting
 *
 *  Copyright 2019 Robert Harris
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
definition(
    name: "HarrisTribe Smart Lighting",
    namespace: "harrisra",
    author: "Robert Harris",
    description: "Smart Lighting",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")



preferences {
	section("Turn on these lights...") {
		input "switches1", "capability.switch", multiple: true
	}
	section("When there's movement...") {
		input "motionSensor1", "capability.motionSensor", title: "Where?"
	}
 	section("And low light is measured here") {
		input "luxSensor1", "capability.illuminanceMeasurement", multiple: false
	}   
	section("And off when there's been no movement for...") {
		input "minutes1", "number", title: "Minutes?"
	}
}


def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	log.debug "Initialising..."
	subscribe(motionSensor1, "motion", motionHandler)
	subscribe(luxSensor1, "illuminance", luxHandler)  
    log.debug "Initialising...Done"
}

def motionHandler(evt) {
	log.debug "$evt.name: $evt.value"
	if (evt.value == "active" && lightingIsNeeded()) {     
		log.debug "turning on lights"
		switches1.on()
	} else if (evt.value == "inactive") {
		runIn(minutes1 * 60, scheduleCheck, [overwrite: false])
	}
}

def lightingIsNeeded() {
 	def currentLuxValue = luxSensor1.currentValue("illuminance")

    log.debug "Current Lux Level is $currentLuxValue"
    
	if (currentLuxValue < 1000) 
		return true
    else 
    	return false
}

def luxHandler(evt) {
	if (!lightingIsNeeded()) {
    	log.debug "turning off lights"
    	switches1.off()
    }
}

def scheduleCheck() {
	log.debug "schedule check"
	def motionState = motion1.currentState("motion")
    if (motionState.value == "inactive") {
        def elapsed = now() - motionState.rawDateCreated.time
    	def threshold = 1000 * 60 * minutes1 - 1000
    	if (elapsed >= threshold) {
            log.debug "Motion has stayed inactive long enough since last check ($elapsed ms):  turning lights off"
            switches1.off()
    	} else {
        	log.debug "Motion has not stayed inactive long enough since last check ($elapsed ms):  doing nothing"
        }
    } else {
    	log.debug "Motion is active, do nothing and wait for inactive"
    }
}
