/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */
import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { PodiumGatewaySharedModule } from '../shared';
import { requestRoute, RequestFormService, RequestFormComponent, RequestFormSubmitDialogComponent } from './';
import { RequestOverviewService, RequestOverviewComponent } from './overview';
import { RequestMainDetailComponent } from './main-detail/request-main-detail.component';
import { RequestDetailComponent } from './main-detail/detail/request-detail.component';
import { RequestActionToolbarComponent } from '../shared/request/action-bars/request-action-toolbar/request-action-toolbar.component';
import { RequestProgressBarComponent } from './main-detail/progress-bar/request-progress-bar.component';
import {
    RequestDraftModalModalComponent
} from './overview/delete-request-draft-modal.component';
import { RequestResolvePagingParams } from './overview/request-overview.route';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { ReviewStatusComponent } from '../shared/review-status/review-status.component';
import { RequestUpdateStatusDialogComponent } from '../shared/status-update/request-update-status-dialog.component';
import { RequestUpdateReviewDialogComponent } from '../shared/status-update/request-update-review-dialog.component';

@NgModule({
    imports: [
        CommonModule,
        PodiumGatewaySharedModule,
        NgbModule,
        RouterModule.forChild(requestRoute)
    ],
    declarations: [
        RequestFormComponent,
        RequestFormSubmitDialogComponent,
        RequestDraftModalModalComponent,
        RequestOverviewComponent,
        RequestMainDetailComponent,
        RequestDetailComponent,
        RequestActionToolbarComponent,
        RequestProgressBarComponent,
        RequestUpdateStatusDialogComponent,
        RequestUpdateReviewDialogComponent,
        ReviewStatusComponent
    ],
    entryComponents: [
        RequestFormSubmitDialogComponent,
        RequestDraftModalModalComponent,
        RequestUpdateStatusDialogComponent,
        RequestUpdateReviewDialogComponent,
        ReviewStatusComponent
    ],
    providers: [
        RequestFormService,
        RequestOverviewService,
        RequestResolvePagingParams
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class PodiumGatewayRequestModule {}
