// Based on JhiAlertService
import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

export type AlertType = 'success' | 'danger' | 'warning' | 'info';

export interface Alert {
    id?: number;
    type: AlertType;
    msg: string;
    params?: any;
    timeout?: number;
    toast?: boolean;
    position?: string;
    scoped?: boolean;
    close?: (alerts: Alert[]) => void;
}

@Injectable({ providedIn: 'root' })
export class AlertService {

    private alertId: number;
    private alerts: Alert[];
    private timeout: number;
    private toast: boolean;
    private i18nEnabled: boolean;

    constructor(
        private translateService: TranslateService
    ) {
        this.toast = true;
        this.i18nEnabled = true;
        this.alertId = 0; // unique id for each alert. Starts from 0.
        this.alerts = [];
        this.timeout = 5000; // default timeout in milliseconds
    }

    clear() {
        this.alerts.splice(0, this.alerts.length);
    }

    get(): Alert[] {
        return this.alerts;
    }

    success(msg: string, params?: any, position?: string): Alert {
        return this.addAlert({
            type: 'success',
            msg,
            params,
            timeout: this.timeout,
            toast: this.isToast(),
            position
        }, []);
    }

    error(msg: string, params?: any, position?: string): Alert {
        return this.addAlert({
            type: 'danger',
            msg,
            params,
            timeout: this.timeout,
            toast: this.isToast(),
            position
        }, []);
    }

    warning(msg: string, params?: any, position?: string): Alert {
        return this.addAlert({
            type: 'warning',
            msg,
            params,
            timeout: this.timeout,
            toast: this.isToast(),
            position
        }, []);
    }

    info(msg: string, params?: any, position?: string): Alert {
        return this.addAlert({
            type: 'info',
            msg,
            params,
            timeout: this.timeout,
            toast: this.isToast(),
            position
        }, []);
    }

    private factory(alertOptions: Alert): Alert {
        const alert: Alert = {
            type: alertOptions.type,
            msg: alertOptions.msg,
            id: alertOptions.id,
            timeout: alertOptions.timeout,
            toast: alertOptions.toast,
            position: alertOptions.position ? alertOptions.position : 'top right',
            scoped: alertOptions.scoped,
            close: (alerts: Alert[]) => {
                return this.closeAlert(alertOptions.id, alerts);
            }
        };
        if (!alert.scoped) {
            this.alerts.push(alert);
        }
        return alert;
    }

    addAlert(alertOptions: Alert, extAlerts?: Alert[]): Alert {
        alertOptions.id = this.alertId++;
        if (this.i18nEnabled && alertOptions.msg) {
            alertOptions.msg = this.translateService.instant(alertOptions.msg, alertOptions.params);
        }
        const alert = this.factory(alertOptions);
        if (alertOptions.timeout && alertOptions.timeout > 0) {
            setTimeout(() => {
                this.closeAlert(alertOptions.id, extAlerts);
            }, alertOptions.timeout);
        }
        return alert;
    }

    closeAlert(id: number, extAlerts?: Alert[]): any {
        const thisAlerts: Alert[] = (extAlerts && extAlerts.length > 0) ? extAlerts : this.alerts;
        return this.closeAlertByIndex(thisAlerts.map((e) => e.id).indexOf(id), thisAlerts);
    }

    closeAlertByIndex(index: number, thisAlerts: Alert[]): Alert[] {
        return thisAlerts.splice(index, 1);
    }

    isToast(): boolean {
        return this.toast;
    }
}
