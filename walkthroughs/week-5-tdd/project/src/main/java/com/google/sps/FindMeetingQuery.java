// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.io.*;


public final class FindMeetingQuery {
    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
        Collection<String> attendees = request.getAttendees();
        Collection<String> opt_attendees = request.getOptionalAttendees();
        long duration = request.getDuration();
        Collection<TimeRange> times = new ArrayList<TimeRange>();

        // The duration of the meeting request is longer than a whole day return no times
        if (duration >= TimeRange.WHOLE_DAY.duration()) {
            return times;
        }

        // Collect the time ranges of events that the attendees have
        List<TimeRange> attendees_ranges = new ArrayList<TimeRange>();
        List<TimeRange> opt_attendees_ranges = new ArrayList<TimeRange>();

        for (Event e : events) {
            Collection<String> event_attendees = e.getAttendees();
            for (String a : attendees) {
                if (event_attendees.contains(a)) {
                    attendees_ranges.add(e.getWhen());
                    break;
                }
            }
            for (String o : opt_attendees) {
                if (event_attendees.contains(o)) {
                    opt_attendees_ranges.add(e.getWhen());
                    break;
                }
            }
        }

        // Sort time ranges by start
        Collections.sort(attendees_ranges, TimeRange.ORDER_BY_START);
        Collections.sort(opt_attendees_ranges, TimeRange.ORDER_BY_START);

        // If the attendees have no events return whole day
        if (attendees_ranges.size() == 0) {
            times.add(TimeRange.WHOLE_DAY);
        }
        else {
            // Find the time ranges where all standard attendees are free
            TimeRange old_r = null;

            for (TimeRange r : attendees_ranges) {

                // The first event so set the range from start of day to event start
                if (old_r == null) {
                    TimeRange new_r = TimeRange.fromStartEnd(0, r.start() - 1, true);
                    if (new_r.duration() >= duration) {
                        times.add(new_r);
                    }
                    old_r = r;
                }
                // If there is no overlap  with the old event set range from end of last range to the start of current event
                else if (old_r.overlaps(r) == false) {
                    TimeRange new_r = TimeRange.fromStartEnd(old_r.end(), r.start() - 1, true);
                    if (new_r.duration() >= duration) {
                        times.add(new_r);
                    }
                    old_r = r;
                }
                // If there is an overlap but not entirely contained set the ending point to be the current range
                else if (old_r.contains(r) == false) {
                    old_r = r;
                }
            }

            // Add the final range to be from the end of the last event to the end of the day
            TimeRange new_r = TimeRange.fromStartEnd(old_r.end(), TimeRange.END_OF_DAY, true);
            if (new_r.duration() >= duration) {
                times.add(new_r);
            }
        }
        if (opt_attendees_ranges.size() != 0) {
            while (opt_attendees_ranges.size() != 0) {
                TimeRange r = opt_attendees_ranges.get(0);
                for (TimeRange t : times) {
                    if (t.contains(r)) {
                        TimeRange before = TimeRange.fromStartEnd(t.start(), r.start(), false);
                        TimeRange after = TimeRange.fromStartEnd(r.end(), t.end(), false);
                        if (before.duration() >= duration) {
                            times.add(before);
                        }
                        if (after.duration() >= duration) {
                            times.add(after);
                        }
                        if (times.size() > 1) {
                            times.remove(t);
                        }
                        break;
                    }
                }
                opt_attendees_ranges.remove(r);
            }
        }
        return times;
    }
}
