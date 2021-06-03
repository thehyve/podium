import { Component, Input } from '@angular/core';

/**
 * A component that will take care of item count statistics of a pagination.
 */
@Component({
    selector: 'jhi-item-count',
    template: ` <div>Showing {{ first }} - {{ second }} of {{ total }} items.</div> `,
})
export class ItemCountComponent {
    @Input() itemsPerPage?: number;
    @Input() page?: number;
    @Input() total?: number;

    get first() {
        let { itemsPerPage, page, total } = this;
        if (page === undefined || total === undefined || itemsPerPage === undefined) {
            return undefined;
        }
        return (page - 1) * itemsPerPage === 0 ? 1 : (page - 1) * itemsPerPage + 1;
    }

    get second() {
        let { itemsPerPage, page, total } = this;
        if (page === undefined || total === undefined || itemsPerPage === undefined) {
            return undefined;
        }
        return page * itemsPerPage < total ? page * itemsPerPage : total;
    }
}
