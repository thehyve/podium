export function makeTranslateServiceMock() {
    let result = {
        currentLang: 'en',
        instant: jest.fn(),
    };
    return result;
}
