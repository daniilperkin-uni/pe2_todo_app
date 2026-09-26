import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import AssigneeForm from './AssigneeForm.vue';

async function submitWith(email: string) {
  const wrapper = mount(AssigneeForm);
  await wrapper.find('input#prename').setValue('Jane');
  await wrapper.find('input#name').setValue('Doe');
  await wrapper.find('input#email').setValue(email);
  await wrapper.find('form').trigger('submit');
  return wrapper;
}

describe('AssigneeForm email validation', () => {
  // The backend only accepts uni-stuttgart.de addresses, so the form rejects
  // everything else before the round trip instead of failing server-side.
  it('rejects a foreign email domain', async () => {
    const wrapper = await submitWith('jane@example.com');

    expect(wrapper.emitted('submit')).toBeUndefined();
    expect(wrapper.text()).toContain('Email must be a uni-stuttgart.de address');
  });

  it('accepts a university subdomain address and trims it', async () => {
    const wrapper = await submitWith('  jane.doe@iste.uni-stuttgart.de  ');

    const submitted = wrapper.emitted('submit');
    expect(submitted).toHaveLength(1);
    expect(submitted?.[0]?.[0]).toMatchObject({ email: 'jane.doe@iste.uni-stuttgart.de' });
  });

  it('requires an email address', async () => {
    const wrapper = await submitWith('');

    expect(wrapper.emitted('submit')).toBeUndefined();
    expect(wrapper.text()).toContain('Email is required.');
  });
});
