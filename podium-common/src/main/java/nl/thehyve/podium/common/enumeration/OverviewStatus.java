/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.enumeration;

/**
 * The OverviewStatus enumeration, used to filter requests by status.
 */
public enum OverviewStatus implements Status {
    All,
    Draft,
    Validation,
    Review,
    Revision,
    Approved,
    Delivery,
    Delivered,
    Partially_Delivered,
    Cancelled,
    Rejected,
    Closed_Approved,
    None;

    static OverviewStatus forClassifier(Classifier classifier) {
        if (classifier == null) {
            return null;
        }
        if (classifier instanceof RequestStatus) {
            RequestStatus status = (RequestStatus)classifier;
            switch(status) {
                case Draft:
                    return Draft;
                case Approved:
                    return Approved;
                case Delivery:
                    return Delivery;
                case None:
                    return None;
                default:
                    throw new RuntimeException("Unexpected status: " + status.toString());
            }
        } else if (classifier instanceof RequestReviewStatus) {
            RequestReviewStatus reviewStatus = (RequestReviewStatus)classifier;
            switch (reviewStatus) {
                case Revision:
                    return Revision;
                case Validation:
                    return Validation;
                case Review:
                    return Review;
                case Closed:
                case None:
                    throw new RuntimeException("Unexpected review status: " + reviewStatus.toString());
            }
        } else if (classifier instanceof RequestOutcome) {
            RequestOutcome outcome = (RequestOutcome)classifier;
            switch (outcome) {
                case Delivered:
                    return Delivered;
                case Partially_Delivered:
                    return Partially_Delivered;
                case Cancelled:
                    return Cancelled;
                case Approved:
                    return Closed_Approved;
                case Rejected:
                    return Rejected;
                case None:
                    throw new RuntimeException("Unexpected outcome: " + outcome.toString());
            }
        }
        throw new RuntimeException("Unexpected classifier: " + classifier.toString());
    }

    public static OverviewStatus forStatus(RequestStatus status, RequestReviewStatus reviewStatus, RequestOutcome outcome) {
        switch(status) {
            case Draft:
            case Approved:
            case Delivery:
            case None:
                return forClassifier(status);
            case Review:
                return forClassifier(reviewStatus);
            case Closed:
                return forClassifier(outcome);
            default:
                throw new RuntimeException("Unexpected status.");
        }
    }

}
