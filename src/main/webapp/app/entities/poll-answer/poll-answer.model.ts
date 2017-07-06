import { BaseEntity } from './../../shared';

export class PollAnswer implements BaseEntity {
    constructor(
        public id?: number,
        public guid?: string,
        public answer?: string,
        public poll?: BaseEntity,
        public pollanswers1S?: BaseEntity[],
    ) {
    }
}
