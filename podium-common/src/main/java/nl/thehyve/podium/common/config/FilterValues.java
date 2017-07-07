package nl.thehyve.podium.common.config;

import nl.thehyve.podium.common.enumeration.OverviewStatus;
import nl.thehyve.podium.common.enumeration.RequestOutcome;
import nl.thehyve.podium.common.enumeration.RequestReviewStatus;
import nl.thehyve.podium.common.enumeration.RequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Definition of the filter values for each of the overview statuses in
 * {@link OverviewStatus}.
 */
public class FilterValues {

    private static final Logger log = LoggerFactory.getLogger(FilterValues.class);

    RequestStatus requestStatus = RequestStatus.None;
    RequestOutcome requestOutcome = RequestOutcome.None;
    RequestReviewStatus reviewStatus = RequestReviewStatus.None;

    private FilterValues(RequestStatus requestStatus) {
        if (requestStatus == RequestStatus.Review || requestStatus == RequestStatus.Closed) {
            throw new RuntimeException("Invalid constructor call. Use review status or outcome instead.");
        }
        this.requestStatus = requestStatus;
    }

    private FilterValues(RequestReviewStatus reviewStatus) {
        this.requestStatus = RequestStatus.Review;
        this.reviewStatus = reviewStatus;
    }

    private FilterValues(RequestOutcome requestOutcome) {
        this.requestStatus = RequestStatus.Closed;
        this.requestOutcome = requestOutcome;
    }

    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    public RequestOutcome getRequestOutcome() {
        return requestOutcome;
    }

    public RequestReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    private static final Map<OverviewStatus, FilterValues> statusFilters = new HashMap<>();
    static {
        statusFilters.put(OverviewStatus.All,                   new FilterValues(RequestStatus.None));
        statusFilters.put(OverviewStatus.Draft,                 new FilterValues(RequestStatus.Draft));
        statusFilters.put(OverviewStatus.Validation,            new FilterValues(RequestReviewStatus.Validation));
        statusFilters.put(OverviewStatus.Review,                new FilterValues(RequestReviewStatus.Review));
        statusFilters.put(OverviewStatus.Revision,              new FilterValues(RequestReviewStatus.Revision));
        statusFilters.put(OverviewStatus.Approved,              new FilterValues(RequestStatus.Approved));
        statusFilters.put(OverviewStatus.Delivery,              new FilterValues(RequestStatus.Delivery));
        statusFilters.put(OverviewStatus.Delivered,             new FilterValues(RequestOutcome.Delivered));
        statusFilters.put(OverviewStatus.Partially_Delivered,   new FilterValues(RequestOutcome.Partially_Delivered));
        statusFilters.put(OverviewStatus.Rejected,              new FilterValues(RequestOutcome.Rejected));
        statusFilters.put(OverviewStatus.Cancelled,             new FilterValues(RequestOutcome.Cancelled));
        statusFilters.put(OverviewStatus.Closed_Approved,       new FilterValues(RequestOutcome.Approved));
    }

    public static FilterValues forStatus(OverviewStatus status) {
        FilterValues filterValues = statusFilters.get(status);
        if (filterValues == null) {
            throw new RuntimeException("No filter values for status " + status.name());
        }
        return filterValues;
    }

}
