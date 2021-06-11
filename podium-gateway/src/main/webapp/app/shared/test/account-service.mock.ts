import { EMPTY } from "rxjs";

export function makeAccountServiceMock() {
    let mocks: any = {
        identity: EMPTY,
        save: EMPTY,
    };
    let result = {
        getAuthenticationState: jest.fn(() => mocks.identity),
        identity: jest.fn(() => mocks.identity),
        save: jest.fn(() => mocks.save),
        
        mockIdentity: (value) => {
            mocks.identity = value;
        },
        mockSave: (value) => {
            mocks.save = value;
        },
    };
    return result;
}
