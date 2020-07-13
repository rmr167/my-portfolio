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
        // Save the request parameters
        Collection<String> attendees = request.getAttendees();
        Collection<String> opt_attendees = request.getOptionalAttendees();
        long duration = request.getDuration();

        // The duration of the meeting request is longer then a whole day return no times
        if (duration >= TimeRange.WHOLE_DAY.duration()) {
            return Collections.emptyList();
        }

        // Create list of time ranges where the attendees are booked
        List<TimeRange> attendees_ranges = new ArrayList<TimeRange>();
        List<TimeRange> opt_attendees_ranges = new ArrayList<TimeRange>();

        for (Event e : events) {
            // Add event range to the attendees_ranges if an attendee is attending that event
            if (overlap_attendees(e, attendees)) {
                attendees_ranges.add(e.getWhen());
            }

            // Add event range to the opt_attendees_ranges if an optional attendee is attending that event
            if (overlap_attendees(e, opt_attendees)) {
                opt_attendees_ranges.add(e.getWhen());
            }
        }

        // Sort time ranges by start
        Collections.sort(attendees_ranges, TimeRange.ORDER_BY_START);
        Collections.sort(opt_attendees_ranges, TimeRange.ORDER_BY_START);

        // Construct an empty list of available time ranges to eventually return
        Collection<TimeRange> times = new ArrayList<TimeRange>();

        // If the non-optional attendees have no events add the entire day to the times
        if (attendees_ranges.size() == 0) {
            times.add(TimeRange.WHOLE_DAY);
        }
        else {
            // Find the time ranges where all standard attendees are free
            times = find_open_times(attendees_ranges, duration);
        }

        // Find the  new list of open time s given the addition of the aoptional attendees
        times = find_open_times_with_optional(opt_attendees_ranges, times, duration);
        
        return times;
    }

    private boolean overlap_attendees(Event e, Collection<String> attendees) {
        Collection<String> event_attendees = e.getAttendees();

        for (String a : attendees) {
            if (event_attendees.contains(a)) {
                return true;
            }
        }
        return false;
    }

    private Collection<TimeRange> find_open_times(List<TimeRange> attendees_ranges, long duration) {
        // Instatiate the return object holding the collection of time ranges where the attendees are free
        Collection<TimeRange> times = new ArrayList<TimeRange>();

        // Intantiate old_r to hold the previous range
        TimeRange old_r = null;

        for (TimeRange r : attendees_ranges) {

            // The first event is set so the range is from start of day to first event start
            if (old_r == null) {
                TimeRange new_r = create_new_range(0, r.start(), false, duration);
                if (new_r != null) {
                    times.add(new_r);
                }
                old_r = r;
            }

            // If there is no overlap  with the old event set range from end of last range to the start of current event
            else if (old_r.overlaps(r) == false) {
                TimeRange new_r = create_new_range(old_r.end(), r.start(), false, duration);
                if (new_r != null) {
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
        TimeRange new_r = create_new_range(old_r.end(), TimeRange.END_OF_DAY, true, duration);
        if (new_r != null) {
            times.add(new_r);
        }
        return times;
    }

    private TimeRange create_new_range(int start, int end, boolean inclusive, long duration) {
        TimeRange r = TimeRange.fromStartEnd(start, end, inclusive);

        if (r.duration() >= duration) {
            return r;
        }

        return null;
    }

    private Collection<TimeRange> find_open_times_with_optional(List<TimeRange> opt_attendees_ranges, Collection<TimeRange> times, long duration) {
        for (TimeRange r : opt_attendees_ranges) {
            for (TimeRange t : times) {
                // If the optional event range is in an open time remove that time range if it is not the only 
                // time range and then add new time ranges 
                if (t.contains(r)) {
                    TimeRange before = create_new_range(t.start(), r.start(), false, duration);
                    if (before != null) {
                        times.add(before);
                    }
                    TimeRange after = create_new_range(r.end(), t.end(), false, duration);
                    if (after != null) {
                        times.add(after);
                    }
                    if (times.size() > 1) {
                        times.remove(t);
                    }
                    break;
                }
            }
        }

        return times;
    }
}
