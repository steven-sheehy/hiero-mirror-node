// SPDX-License-Identifier: Apache-2.0

import {ContractCallTestScenarioBuilder} from './common.js';

const contract = __ENV.ERC_CONTRACT_ADDRESS;
const selector = '0x01984892';
const token = __ENV.TOKEN_ADDRESS;

const {options, run} = new ContractCallTestScenarioBuilder()
  .name('contractCallName') // use unique scenario name among all tests
  .selector(selector)
  .args([token])
  .to(contract)
  .build();

export {options, run};
